package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.CommentRequestDto;
import com.Connectify.Connectify.dto.ReplyDto;
import com.Connectify.Connectify.entity.Comment;
import com.Connectify.Connectify.entity.Post;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.repository.IComment;
import com.Connectify.Connectify.repository.IPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private IComment iComment;

    @Autowired
    private IPost iPost;


    public ResponseEntity<String> addComment(CommentRequestDto commentDetails, UserPrinciple userDetails) {
        User userId = userDetails.getUser();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You can not add comment");
        }

        Long postId = commentDetails.getPostId();
        //finding post

        Optional<Post> existingPost = iPost.findById(postId);
        if(existingPost.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("post not found in which you want to add this comment");
        }

        //checking validations
        String commentBody = commentDetails.getText();
        if (commentBody.isEmpty()|| commentBody.length() > 400){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please add comment between 1 to 500 character");
        }

        // creating a comment object
        Comment comment = new Comment();
        comment.setText(commentBody);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setPost(existingPost.get());
        comment.setUser(userId);
        comment.setParentComment(null);

        try{
            iComment.save(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something went wrong please try again latter");
        }

       return ResponseEntity.status(HttpStatus.CREATED).body("comment added");
    }

    public ResponseEntity<String> addReply(Long commentId, CommentRequestDto commentDetails, UserPrinciple userDetails) {
        User user = userDetails.getUser();
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to reply");
        }

        Long existingPostId = commentDetails.getPostId();
        Optional<Post>post = iPost.findById(existingPostId);
        if(post.isEmpty()){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("post not found in which you want to reply the comment");
        }

        Optional<Comment> existingComment = iComment.findById(commentId);
        if(existingComment.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("comment not found which you want to reply");
        }

        String commentBody = commentDetails.getText();
        if (commentBody.isEmpty()|| commentBody.length() > 400){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please add comment between 1 to 500 character");
        }


        // creating a comment object
        Comment comment = new Comment();
        comment.setText(commentBody);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setPost(post.get());
        comment.setUser(user);
        comment.setParentComment(existingComment.get());


        try {
            iComment.save(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something went wrong please try again latter");
        }


        return  ResponseEntity.status(HttpStatus.CREATED).body("Your reply added successfully");



    }

    public ResponseEntity<?> getAllReplyOfPost(Long commentId, UserPrinciple userDetails) {
        User user = userDetails.getUser();
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized ");
        }

        Optional<Comment> existingComment = iComment.findById(commentId);
        if(existingComment.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("comment not found ");
        }

        List<Comment> allReplyComments = iComment.findAllByParentComment(existingComment.get());


        if (allReplyComments.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body("No replies found");
        }
        List<ReplyDto> allReplies  = allReplyComments.stream().map(comment -> {
            ReplyDto replies = new ReplyDto();
             replies.setId(comment.getId());
             replies.setText(comment.getText());
             replies.setCreatedAt(comment.getCreatedAt());
             replies.setUserId(comment.getUser().getId());
             replies.setUserName(comment.getUser().getUserName());
             replies.setUserprofilePicture(comment.getUser().getProfilePictureUrl());
             replies.setParentCommentId(comment.getParentComment().getId());
             return  replies;
        }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(allReplies);
    }

    public ResponseEntity<?> getNoOfReplies(Long commentId) {
        Optional<Comment>parentComment = iComment.findById(commentId);
        if (parentComment.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        List<Comment> allReplies = iComment.findAllByParentComment(parentComment.get());
        Integer numberOfReplies = allReplies.size();

        return ResponseEntity.status(HttpStatus.OK).body(numberOfReplies);
    }

    public ResponseEntity<?> getAllComments(Long postId) {
        Optional<Post> existingPost = iPost.findById(postId);
        if (existingPost.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        List<Comment> allParentComment = iComment.findAllByParentCommentAndPost(null,existingPost.get());


        List<ReplyDto> allReplies  = allParentComment.stream().map(comment -> {
            ReplyDto replies = new ReplyDto();
            replies.setId(comment.getId());
            replies.setText(comment.getText());
            replies.setCreatedAt(comment.getCreatedAt());
            replies.setUserId(comment.getUser().getId());
            replies.setUserName(comment.getUser().getUserName());
            replies.setUserprofilePicture(comment.getUser().getProfilePictureUrl());
            replies.setParentCommentId(null);
            return  replies;
        }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(allReplies);
    }

    public ResponseEntity<Integer> getTotalNumberOfCommets(Long postId) {
        Optional<Post> existingPost = iPost.findById(postId);
        if (existingPost.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0);
        }
      Integer totalComments = iComment.findAllByParentCommentAndPost(null,existingPost.get()).size();
      return  ResponseEntity.status(HttpStatus.OK).body(totalComments);

    }

    public ResponseEntity<String> editComment(Long commentId, UserPrinciple userDetails, String text) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Text cannot be null or empty");
        }

        if (text.isEmpty()|| text.length() > 400){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please add comment between 1 to 400 character");
        }

        Optional<Comment> existingComment = iComment.findById(commentId);
        if (existingComment.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("comment not found");
        }

        //verifying user  means the user can only edit comment who made this comment
        if (!existingComment.get().getUser().getId().equals(userDetails.getId())){
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to edit this comment");
        }

        existingComment.get().setText(text);

        try{
            iComment.save(existingComment.get());
        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again latter");
        }

        return  ResponseEntity.status(HttpStatus.OK).body("Comment update successfully");

    }

    public ResponseEntity<String> deleteComment(Long commentId, UserPrinciple userDetails) {
        Optional<Comment> comment = iComment.findById(commentId);

        if (comment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        // Check if the user is authorized to delete the comment
        if (!comment.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to delete this comment");
        }

        try {
            iComment.delete(comment.get());
            return ResponseEntity.status(HttpStatus.OK).body("Comment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the comment");
        }
    }
}

//note corrrect the get all reply
