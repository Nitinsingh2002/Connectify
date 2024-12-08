package com.Connectify.Connectify.entity;
import com.Connectify.Connectify.enums.AccountType;
import com.Connectify.Connectify.enums.Gender;
import com.Connectify.Connectify.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;



@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 50,message = "Username cannot be longer than 50 characters")
    @Size(min = 3,message = "Username cannot be shorter than 3 characters")
    @Column(nullable = false,unique = true,length = 50)
    private  String userName;

    @Size(max = 100, message = "Full name cannot be longer than 100 characters")
    @Column(length = 100)
    private String fullName;

    @Size(max = 255, message = "Bio cannot be more than 255 characters")
    @Column (length = 255)
    private String bio;


    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false,unique = true,length = 100)
    private String email;

    @NotBlank(message = "Please add a strong password")
    @Column(nullable = false)
    private String password;


    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType = AccountType.PUBLIC;


    @Column(nullable = false)
    private boolean verified = false;


    @NotNull(message = "Creation time must be specified")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;



    @Column(length = 255)
    private String profilePictureUrl;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;


    private  String ResetPasswordToken;

    private LocalDateTime resetPasswordExpires;
}
