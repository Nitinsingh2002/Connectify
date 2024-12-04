package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.dto.UserDto;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.enums.AccountType;
import com.Connectify.Connectify.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //Api to register user
    @PostMapping("/register")
    private ResponseEntity<String> registerUser(@RequestBody   User user) {
        return userService.register(user);
    }


    //Api to check username available or not
    @GetMapping("/check-username/{username}")
    private ResponseEntity <String> checkUsername(@PathVariable String username ){
        return userService.checkUsername(username);
    }


    //Api to View Profile
    @GetMapping("/{id}")
    private ResponseEntity<?> getProfile(@PathVariable Long id){
        return userService.getProfile(id);
    }


   //Api to update user details
    @PutMapping("/{id}")
    private ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDto userDetails){
        return userService.updateUser(id,userDetails);
    }

    //Api to convert account type
    @PutMapping("/{id}/{type}")
    private ResponseEntity<String> changeAccountType(@PathVariable Long id, @PathVariable AccountType type){
        return userService.changeAccountType(id,type);
    }



}
