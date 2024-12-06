package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.dto.LoginDto;
import com.Connectify.Connectify.dto.UserDto;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.enums.AccountType;
import com.Connectify.Connectify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
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
    private ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDto userDetails,
                                              @AuthenticationPrincipal UserPrinciple userPrinciple){
        return userService.updateUser(id,userDetails,userPrinciple);
    }

    //Api to convert account type
    @PutMapping("/{id}/{type}")
    private ResponseEntity<String> changeAccountType(@PathVariable Long id, @PathVariable AccountType type,
                                                     @AuthenticationPrincipal UserPrinciple userPrinciple){
        return userService.changeAccountType(id,type,userPrinciple);
    }

    //Api to login
    @PostMapping("/login")
    private ResponseEntity<String> userLogin(@RequestBody LoginDto loginDetails){
        return userService.verify(loginDetails);
    }



}
