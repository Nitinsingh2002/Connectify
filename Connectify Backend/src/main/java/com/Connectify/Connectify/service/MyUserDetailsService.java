package com.Connectify.Connectify.service;


import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.repository.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService  implements UserDetailsService {

    @Autowired
    IUser iUser;




    //this is abstract method  we need to define this method when we implement UserDetails Service
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = iUser.findByEmail(email);

        if(user == null) {
            System.out.println("user not found");
            throw new UsernameNotFoundException("user not found");
        }

        //since UserDetails is interface, and we have to return  UserDetails so we create a class
        //UserPrinciple that implement UserDetails interface. we create that class in controller folder
        return new UserPrinciple(user);
    }



}