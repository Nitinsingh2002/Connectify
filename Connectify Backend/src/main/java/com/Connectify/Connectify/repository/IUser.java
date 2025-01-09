package com.Connectify.Connectify.repository;

import com.Connectify.Connectify.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IUser extends JpaRepository<User,Long> {

    User findByEmail(String email);
    boolean existsByUserName(String username);

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email);


    User findByUserName(String username);

    List<User> findByUserNameIgnoreCaseContainingOrFullNameIgnoreCaseContaining(String query, String query1);
}
