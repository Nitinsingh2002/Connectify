//package com.Connectify.Connectify.config;
//
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(AbstractHttpConfigurer::disable);
//        http.authorizeHttpRequests(auth ->{
//            auth.requestMatchers("/api/v1/user/register").permitAll();
//            auth.anyRequest().authenticated();
//        });
//        http.formLogin(Customizer.withDefaults());
//        http.httpBasic(Customizer.withDefaults());
//        return http.build();
//    }
//}
