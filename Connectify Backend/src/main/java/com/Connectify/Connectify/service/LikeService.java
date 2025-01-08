package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.LikeDto;
import com.Connectify.Connectify.entity.*;
import com.Connectify.Connectify.enums.LikeType;
import com.Connectify.Connectify.repository.IComment;
import com.Connectify.Connectify.repository.ILIKE;
import com.Connectify.Connectify.repository.IPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class LikeService {
    @Autowired
    ILIKE ilike;

    @Autowired
    IPost iPost;

    @Autowired
    IComment iComment;

    public ResponseEntity<String> addPostLike(Long postId, UserPrinciple userDetails) {
        Optional<Post> existingPost = iPost.findById(postId);

        if (existingPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Optional<Like> existingLike = ilike.findByPostAndUser(existingPost.get(), userDetails.getUser());

        try {
            if (existingLike.isEmpty()) {
                // Add like
                Like like = new Like();
                like.setLikeType(LikeType.POST);
                like.setUser(userDetails.getUser());
                like.setPost(existingPost.get());
                ilike.save(like);
                return ResponseEntity.status(HttpStatus.OK).body("Post liked successfully");
            } else {
                // Remove like
                ilike.delete(existingLike.get());
                return ResponseEntity.status(HttpStatus.OK).body("Post unliked successfully");
            }
        } catch (Exception e) {
            // Log the error for debugging (optional)
            // logger.error("Error toggling like for postId {}: {}", postId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, please try again later");
        }
    }



    
    public ResponseEntity<String> likeComment(Long commentId, UserPrinciple userDetails) {
        Optional<Comment> existingComment = iComment.findById(commentId);

        if(existingComment.isEmpty()){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        Optional<Like> existingLike = ilike.findByCommentAndUser(existingComment.get(),userDetails.getUser());

        try {
            if (existingLike.isEmpty()) {
                // Add like
                Like like = new Like();
                like.setLikeType(LikeType.COMMENT);
                like.setUser(userDetails.getUser());
                like.setComment(existingComment.get());
                ilike.save(like);
                return ResponseEntity.status(HttpStatus.OK).body("comment liked successfully");
            } else {
                // Remove like
                ilike.delete(existingLike.get());
                return ResponseEntity.status(HttpStatus.OK).body("comment unliked successfully");
            }
        } catch (Exception e) {
            // Log the error for debugging (optional)
            // logger.error("Error toggling like for postId {}: {}", postId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, please try again later");
        }
    }


    public ResponseEntity<?> postLikeDetails(Long postId) {
        Optional<Post> existingPost = iPost.findById(postId);

        if (existingPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        List<Like> allLikes = ilike.findByPost(existingPost.get());

        if (allLikes.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList());
        }

        //mapping to like dto
        List<LikeDto> allLikesDetails = allLikes.stream().map( like -> {
            LikeDto likeRecord = new LikeDto();
            likeRecord.setId(like.getId());
            likeRecord.setUserName(like.getUser().getUserName());
            likeRecord.setUserId(like.getUser().getId());
            likeRecord.setUserProfilePicture(like.getUser().getProfilePictureUrl());
            return likeRecord;
        }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(allLikesDetails);
    }

    public ResponseEntity<?> postLikeCount(Long postId) {
        Optional<Post> existingPost = iPost.findById(postId);

        if (existingPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        List<Like> allLikes = ilike.findByPost(existingPost.get());

        if (allLikes.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(0);
        }
        return ResponseEntity.status(HttpStatus.OK).body(allLikes.size());
    }

    public ResponseEntity<?> commentLikeCount(Long commentId) {
        Optional<Comment> existingComment = iComment.findById(commentId);

        if (existingComment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("comment not found");
        }

        List<Like> allLikes = ilike.findByComment(existingComment.get());

        if (allLikes.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(0);
        }
        return ResponseEntity.status(HttpStatus.OK).body(allLikes.size());
    }
}
