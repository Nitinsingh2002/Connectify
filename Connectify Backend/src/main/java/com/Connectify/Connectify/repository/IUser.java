package com.Connectify.Connectify.repository;

import com.Connectify.Connectify.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IUser extends JpaRepository<User,Long> {

    User findByEmail(String email);
    boolean existsByUserName(String username);

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email);
}
