package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.dto.PostRequestDto;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1")
@RestController
public class PostController {
    @Autowired
    PostService postService;

    //api to create post
    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    private ResponseEntity<String> addPost(@ModelAttribute PostRequestDto postDetails,
                                           @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return postService.addPost(postDetails, userPrinciple);
    }

    //api to update post caption
    @PutMapping("/post/{postId}/{caption}")
    private ResponseEntity<String> updateCaption(@PathVariable String caption,
                                                 @AuthenticationPrincipal UserPrinciple userPrinciple,
                                                 @PathVariable Long postId) {
        return postService.updateCaption(caption, userPrinciple, postId);
    }


    //api to delete post
    @DeleteMapping("/post/{postId}")
    private ResponseEntity<String> deletePost(@PathVariable Long postId,
                                              @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return postService.deletePost(postId, userPrinciple);
    }


    //api to get single post
    @GetMapping("/post/{postId}")
    private ResponseEntity<?> getSinglePost(@PathVariable Long postId) {
        return postService.getSinglePost(postId);
    }


    //api to get all post of single user
    @GetMapping("/posts/{userId}")
    private ResponseEntity<?> getAllPost(@PathVariable Long userId) {
        return postService.getAllPost(userId);
    }


    // api to search a post 
}
