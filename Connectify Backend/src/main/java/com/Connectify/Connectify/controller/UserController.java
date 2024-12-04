package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //Api to register user
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody   User user) {
        return userService.register(user);
    }


}
