package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.LoginDto;
import com.Connectify.Connectify.dto.UserDto;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    IUser iUser;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;


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
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An unexpected error occurred, please try again later");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully");
    }


    public ResponseEntity<String> checkUsername(String username) {
        try {
            if (username.length() < 4 || username.length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must be between 4 to 50 characters");
            }

            boolean userName = iUser.existsByUserName(username.toLowerCase());

            if (userName) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Username available");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred, please try again later");
        }
    }


    public ResponseEntity<?> getProfile(Long id) {
        Optional<User> user = iUser.findById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found");
        }

        try {
            User retrievedUser = user.get();
            UserDto userDetails = new UserDto(
                    retrievedUser.getId(),
                    retrievedUser.getUserName(),
                    retrievedUser.getFullName(),
                    retrievedUser.getBio(),
                    retrievedUser.getEmail(),
                    retrievedUser.getGender()
            );
            return ResponseEntity.status(HttpStatus.OK).body(userDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again later");
        }
    }


    public ResponseEntity<String> updateUser(Long id, @Valid UserDto userDetails) {
        // Check if user exists
        Optional<User> user = iUser.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = user.get();

        // Validate username
        if (!userDetails.getUserName().equals(existingUser.getUserName())) {
            boolean userNameExists = iUser.existsByUserName(userDetails.getUserName());
            if (userNameExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
            }
        }

        // Validate full name length
        String fullName = userDetails.getFullName();
        if (fullName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Full name cannot be empty");
        }
        if (fullName.length() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Full name cannot exceed 100 characters");
        }

        // Validate bio length
        if (userDetails.getBio().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bio cannot be empty");
        }
        if (userDetails.getBio().length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bio cannot exceed 255 characters");
        }

        // Validate email
        if (!userDetails.getEmail().equals(existingUser.getEmail())) {
            boolean userEmailExists = iUser.existsByEmail(userDetails.getEmail());
            if (userEmailExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already taken");
            }
        }

        existingUser.setUserName(userDetails.getUserName());
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setBio(userDetails.getBio());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setGender(userDetails.getGender());

        try {
            iUser.save(existingUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, please try again later");
        }
        return ResponseEntity.status(HttpStatus.OK).body("User updated successfully");
    }


    public ResponseEntity<String> changeAccountType(Long id, AccountType type) {
        Optional<User> user = iUser.findById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User retrivedUser = user.get();
        retrivedUser.setAccountType(type);

        try {
            iUser.save(retrivedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again letter");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Account type changed successfully!");
    }




    public ResponseEntity<String> verify(LoginDto loginDetails) {

        String  username  = loginDetails.getUsername();
        String password = loginDetails.getPassword();


        Authentication authentication =
                authManager.authenticate
                        (new UsernamePasswordAuthenticationToken(loginDetails.getUsername(), loginDetails.getPassword()));


        if (authentication.isAuthenticated()) {
            System.out.println("true");
            String token = jwtService.getToken(loginDetails.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials");
    }
}
