package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.LoginDto;
import com.Connectify.Connectify.dto.ResetPasswordDto;
import com.Connectify.Connectify.dto.UserDto;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
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
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    IUser iUser;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;


    @Autowired
    EmailService emailService;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final BCryptPasswordEncoder resetTokenEncoder = new BCryptPasswordEncoder(4);

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


    public ResponseEntity<String> updateUser(Long id, @Valid UserDto userDetails, UserPrinciple userPrinciple) {

        //extracting email
        //actually UserPrinciple object is also object of UserDetails because it implements UserDetails interface
        //and in UserPrinciple class we have method getUsername //since we are using authentication using gmail
        //so it returns email
        String emailFromToken = userPrinciple.getUsername();


        // Check if user exists
        Optional<User> user = iUser.findById(id);


        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = user.get();

        //checking that only login user can update their details
        if (!emailFromToken.equals(existingUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("you are not authorized to update details");
        }


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

        // Validate email means if user update email first check that email is available or not
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


    public ResponseEntity<String> changeAccountType(Long id, AccountType type, UserPrinciple userPrinciple) {
        Optional<User> user = iUser.findById(id);

        //extracting email from userPrinciple //since we use authentication using email so username return email
        //for more clarity check userPrinciple class
        String email = userPrinciple.getUsername();


        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User retrivedUser = user.get();

        //checking only login user can change their account type
        if (!email.equals(retrivedUser.getEmail())) {
            return ResponseEntity.status((HttpStatus.UNAUTHORIZED)).body("You are not authorised change account type");
        }
        retrivedUser.setAccountType(type);

        try {
            iUser.save(retrivedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again letter");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Account type changed successfully!");
    }


    public ResponseEntity<String> verify(LoginDto loginDetails) {

        String password = loginDetails.getPassword();

        try{
            Authentication authentication =
                    authManager.authenticate
                            (new UsernamePasswordAuthenticationToken(loginDetails.getEmail(), loginDetails.getPassword()));


            if (authentication.isAuthenticated()) {
                // Retrieve user ID (assuming MyUserDetails has a getId() method)
                UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
                Long userId = userDetails.getId();
                //getting token
                String token = jwtService.getToken(loginDetails.getEmail(), userId);
                return ResponseEntity.status(HttpStatus.OK).body(token);
            } else {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials");
        }


    }

    public ResponseEntity<String> forgotPassword(String email) {

        //finding user with email
        User user = iUser.findByEmail(email);

        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found with this email");
        }

        //creating token
        String token  = UUID.randomUUID().toString();

        // we are hashing token with the help of bcrypt with less  round so we create new instance of bcrypt
        String hashedToken = resetTokenEncoder.encode(token);

        //saving the token and token expiration time
        user.setResetPasswordToken(hashedToken);
        LocalDateTime time = LocalDateTime.now().plusMinutes(10);
        user.setResetPasswordExpires(time);
         try{
             iUser.save(user);
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again letter");
         }

         //sending mail to user
        String to = email;
        String subject = "Forgot password request";
        String body = "You requested a password reset. Please click on the link below to reset your password:\n\n" +
                "http://localhost:3000/reset-password/" +user.getId() +"/"+ token + "\n\n" +
                "If you did not request this, please ignore this email.";
       try{
           emailService.sendMail(to,subject,body);
       }catch (Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email.");
       }

       return ResponseEntity.status(HttpStatus.OK).body("If the email exists, a reset link has been sent.");

    }

    public ResponseEntity<String> resetPassword(String token, ResetPasswordDto pass, Long userId) {

        //finding user with user id
        Optional<User> optionalUser = iUser.findById(userId);

       if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid or expired reset token.");
       }

       User user = optionalUser.get();

        // Check if reset token or expiration fields are null  we are checking null because isBefore method not handle null
        if (user.getResetPasswordToken() == null || user.getResetPasswordExpires() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid reset token.");
        }

       //comparing token from db and provided token
        boolean match = resetTokenEncoder.matches(token,user.getResetPasswordToken());

        if(!match){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset token has expired.");
        }

        // Check if the token has expired
        if (user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset token has expired.");
        }
        user.setPassword(null);


        String hashedPassword = encoder.encode(pass.getPassword());

        System.out.println(hashedPassword);

        user.setPassword(hashedPassword);

       user.setResetPasswordExpires(null);
       user.setResetPasswordToken(null);

      try{
         iUser.save(user);
      }catch (Exception e){
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                  body("something went wrong please try again latter");
      }

        return ResponseEntity.status(HttpStatus.OK).body("Password has been successfully reset.");
    }
}
