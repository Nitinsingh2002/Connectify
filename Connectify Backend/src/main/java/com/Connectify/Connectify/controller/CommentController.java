package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.dto.CommentRequestDto;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/post")
@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;


    //api to add a comment (parent comment)
    @PostMapping("/comment")
    private ResponseEntity<String> addComment(@RequestBody CommentRequestDto comment,
                                              @AuthenticationPrincipal UserPrinciple userDetails) {
        return commentService.addComment(comment, userDetails);
    }


    //api to reply a comment
    @PostMapping("/{commentId}/reply")
    private ResponseEntity<String> addReply(@PathVariable Long commentId,
                                            @RequestBody CommentRequestDto comment,
                                            @AuthenticationPrincipal UserPrinciple userDetails) {
        return commentService.addReply(commentId, comment, userDetails);
    }

    //api to get all replies of comment
    @GetMapping("/{commentId}/reply")
    private ResponseEntity<?> commentReply(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrinciple userDetails
    ) {
        return commentService.getAllReplyOfPost(commentId, userDetails);
    }

    // api to get count of all replies of a comment
    @GetMapping("/comment/{commentId}")
    private ResponseEntity<?> getNoOfReplies(@PathVariable Long commentId) {
        return commentService.getNoOfReplies(commentId);
    }

    //api to get all comment of a post
    @GetMapping("/all-comments/{postId}")
    private ResponseEntity<?> getAllComments(@PathVariable Long postId) {
        return commentService.getAllComments(postId);
    }

    //api to get count of all comment of a post
    @GetMapping("/all-comment/{postId}")
    private ResponseEntity<Integer> getAllComment(@PathVariable Long postId) {
        return commentService.getTotalNumberOfCommets(postId);
    }


    //api to delete a comment
    //api to delete a reply
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                @AuthenticationPrincipal UserPrinciple userDetails) {
        return  commentService.deleteComment(commentId,userDetails);
    }

    //api to edit a comment
    @PutMapping("/comment/{commentId}")
    private ResponseEntity<String> editComment(@PathVariable Long commentId,
                                                  @AuthenticationPrincipal UserPrinciple userDetails,
                                               @RequestBody  String text){
        return  commentService.editComment(commentId,userDetails,text);
    }



}
