package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.service.LikeService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LikeController {
    @Autowired
    LikeService likeService;
    //api to like a post
    @PostMapping("/post/like/{postId}")
    private ResponseEntity<String> likePost(@PathVariable Long postId,
                                            @AuthenticationPrincipal UserPrinciple userDetails){
        return  likeService.addPostLike(postId,userDetails);
    }

    //api to like a comment
    @PostMapping("/comment/like/{commentId}")
    private ResponseEntity<String> likeComment(@PathVariable Long commentId,
                                               @AuthenticationPrincipal UserPrinciple userDetails){
        return likeService.likeComment(commentId,userDetails);
    }


    //api to get Details of all like of post
    @GetMapping("/like/details/{postId}")
    private ResponseEntity<?> postLikeDetails (@PathVariable Long postId){
        return likeService.postLikeDetails(postId);
    }

    // api to get all likes of a post
    @GetMapping("/like/count/{postId}")
    private ResponseEntity<?> postLikeCount (@PathVariable Long postId){
        return likeService.postLikeCount(postId);
    }

    //api to get count of all like of a comment
    @GetMapping("/like/comment/count/{commentId}")
    private ResponseEntity<?> commentLikeCount (@PathVariable Long commentId){
        return likeService.commentLikeCount(commentId);
    }

}








