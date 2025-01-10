package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.service.ExploreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class ExploreController {
    @Autowired
    ExploreService exploreService;
    //api to search post

    // api to search user
    @GetMapping("/search/user")
    private ResponseEntity<?> searchUser(@RequestParam String query,
                                         @AuthenticationPrincipal UserPrinciple userDetails) {
        return exploreService.searchUser(query, userDetails);
    }


    // api to search for  post
    @GetMapping("/search/post")

    private ResponseEntity<?> postSearch(@RequestParam("query") String query,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        //Note page start from 0
        return exploreService.searchPost(query, page, size);
    }

    //api to create timeline for user
     @GetMapping("/timeline")
    private ResponseEntity<?> createTimeLine(@RequestParam(value = "page", defaultValue = "0")int page,
                                             @RequestParam(value="size",defaultValue = "20")int size,
                                             @AuthenticationPrincipal UserPrinciple userDetails){
        return exploreService.createTimeline(page,size,userDetails);
    }

}
