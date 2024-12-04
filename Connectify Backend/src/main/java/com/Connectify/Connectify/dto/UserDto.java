package com.Connectify.Connectify.dto;

import com.Connectify.Connectify.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {

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

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
}
