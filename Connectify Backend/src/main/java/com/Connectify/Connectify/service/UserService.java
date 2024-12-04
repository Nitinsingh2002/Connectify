package com.Connectify.Connectify.service;


import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.enums.AccountType;
import com.Connectify.Connectify.enums.Role;
import com.Connectify.Connectify.repository.IUser;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    IUser iUser;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ResponseEntity<String> register(@Valid User user) {
        if (user.getPassword().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Password must be at least 8 characters long");
        }

        String email = user.getEmail();
        User existingUser = iUser.findByEmail(email);
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email ID already used");
        }

        user.setId(null);
        String password = user.getPassword();
        String hashedPassword = encoder.encode(password);
        user.setPassword(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);
        user.setAccountType(AccountType.PUBLIC);
        user.setUserName(user.getUserName().toLowerCase());

        try {
            iUser.save(user);
        } catch (ConstraintViolationException e) {
            StringBuilder errorMessages = new StringBuilder();
            e.getConstraintViolations().forEach(violation -> {
                errorMessages.append(violation.getPropertyPath())
                        .append(" : ")
                        .append(violation.getMessage())
                        .append(";");
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages.toString());
        } catch (DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An unexpected error occurred");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully");
    }




    
}
