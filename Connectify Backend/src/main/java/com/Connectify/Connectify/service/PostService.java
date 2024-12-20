package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.PostRequestDto;
import com.Connectify.Connectify.dto.PostResponseDto;
import com.Connectify.Connectify.dto.UserDto;
import com.Connectify.Connectify.entity.Post;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.repository.IPost;
import com.Connectify.Connectify.repository.IUser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    IPost iPost;

    @Autowired
    IUser iUser;

    @Autowired
    CloudinaryService cloudinaryService;

    public ResponseEntity<String> addPost(@Valid PostRequestDto postDetails, UserPrinciple userPrinciple) {
        // Get the current user
        User currentUser = userPrinciple.getUser();


        // we have to verify tagged user means user is available or not


        String postContentUrl = null;
        String thumbnailUrl = null;

        try {

            postContentUrl = cloudinaryService.upload(postDetails.getPostContentFile());

            if (postDetails.getThumbnailFile() != null) {
                thumbnailUrl = cloudinaryService.upload(postDetails.getThumbnailFile());
            }
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File upload failed !! " + exception.getMessage());
        }

        try {
            // Create Post entity
            Post post = new Post();
            post.setCaption(postDetails.getCaption());
            post.setPostType(postDetails.getPostType());
            post.setCreatedAt(LocalDateTime.now());
            post.setPostContent(postContentUrl);
            post.setThumbnailUrl(thumbnailUrl);
            post.setUser(currentUser);
            post.setTaggedUSer(postDetails.getTaggedUser());


            iPost.save(post);
            return ResponseEntity.status(HttpStatus.CREATED).body("Post added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while saving the post. Please try again later.");
        }
    }

    public ResponseEntity<String> updateCaption(String caption, UserPrinciple userPrinciple, Long postId) {
        Long currentUserId = userPrinciple.getId();


        Optional<Post> currentPost = iPost.findById(postId);

        if (caption != null && caption.length() > 200) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Caption should be less than 200 characters.");
        }


        if (currentPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Post post = currentPost.get();

        if (!post.getUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to update post");
        }

        post.setCaption(caption);

        try {
            iPost.save(post);
            return ResponseEntity.status(HttpStatus.OK).body("Post updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("something went wrong, please try agan latter");

        }

    }

    public ResponseEntity<String> deletePost(Long postId, UserPrinciple userPrinciple) {
        Optional<Post> currentPost = iPost.findById(postId);

        if (currentPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Post post = currentPost.get();

        Long loginUserId = userPrinciple.getId();
        if (!loginUserId.equals(post.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this post");
        }

        try {
            iPost.delete(post);
            return ResponseEntity.status(HttpStatus.OK).body("Post deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("something went wrong , please try again latter");
        }
    }

    public ResponseEntity<?> getSinglePost(Long postId) {
        Optional<Post> currentPost = iPost.findById(postId);

        if (currentPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Post post = currentPost.get();

        //creating post response sto
        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.setId(post.getId());
        postResponseDto.setCaption(post.getCaption());
        postResponseDto.setPostContentUrl(post.getPostContent());
        postResponseDto.setThumbnailUrl(post.getThumbnailUrl());
        postResponseDto.setCreatedAt(post.getCreatedAt());
        postResponseDto.setUpdatedAt(post.getUpdatedAt());
        postResponseDto.setPostType(post.getPostType());

        //setting user dto
        UserDto userDto = new UserDto();
        userDto.setId(post.getUser().getId());
        userDto.setUserName(post.getUser().getUserName());
        userDto.setFullName(post.getUser().getFullName());
        userDto.setBio(post.getUser().getBio());
        userDto.setEmail(post.getUser().getEmail());
        userDto.setGender(post.getUser().getGender());

        postResponseDto.setUser(userDto);


        //handling tagged user
        Set<UserDto> taggedUsersDto = post.getTaggedUSer()
                .stream()
                .map(taggedUser -> {
                    UserDto taggedUserDto = new UserDto();
                    taggedUserDto.setId(taggedUser.getId());
                    taggedUserDto.setUserName(taggedUser.getUserName());
                    taggedUserDto.setFullName(taggedUser.getFullName());
                    taggedUserDto.setBio(taggedUser.getBio());
                    taggedUserDto.setEmail(taggedUser.getEmail());
                    taggedUserDto.setGender(taggedUser.getGender());
                    return taggedUserDto;
                })
                .collect(Collectors.toSet());
        postResponseDto.setTaggedUser(taggedUsersDto);

        return ResponseEntity.status(HttpStatus.OK).body(postResponseDto);
    }

    public ResponseEntity<?> getAllPost(Long userId) {

        Optional<User> userDetails = iUser.findById(userId);

        if (userDetails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userDetails.get();

        try {
            // Finding all posts related to a user
            List<Post> allPost = iPost.findAllByUser(user);

            if (allPost == null || allPost.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList());
            }

            // Converting List<Post> to List<PostResponseDto>
            List<PostResponseDto> allPostResponse = allPost.stream().map(post -> {
                PostResponseDto postResponseDto = new PostResponseDto();
                postResponseDto.setId(post.getId());
                postResponseDto.setCaption(post.getCaption());
                postResponseDto.setPostContentUrl(post.getPostContent());
                postResponseDto.setThumbnailUrl(post.getThumbnailUrl());
                postResponseDto.setCreatedAt(post.getCreatedAt());
                postResponseDto.setUpdatedAt(post.getUpdatedAt());

                // Setting UserDto for the post
                UserDto userDto = new UserDto();
                userDto.setId(post.getUser().getId());
                userDto.setUserName(post.getUser().getUserName());
                userDto.setFullName(post.getUser().getFullName());
                userDto.setBio(post.getUser().getBio());
                userDto.setEmail(post.getUser().getEmail());
                userDto.setGender(post.getUser().getGender());
                postResponseDto.setUser(userDto);

                // Handling Tagged Users
                Set<UserDto> taggedUsersDto = post.getTaggedUSer()
                        .stream()
                        .map(taggedUser -> {
                            UserDto taggedUserDto = new UserDto();
                            taggedUserDto.setId(taggedUser.getId());
                            taggedUserDto.setUserName(taggedUser.getUserName());
                            taggedUserDto.setFullName(taggedUser.getFullName());
                            taggedUserDto.setBio(taggedUser.getBio());
                            taggedUserDto.setEmail(taggedUser.getEmail());
                            taggedUserDto.setGender(taggedUser.getGender());
                            return taggedUserDto;
                        })
                        .collect(Collectors.toSet());
                postResponseDto.setTaggedUser(taggedUsersDto);

                return postResponseDto;
            }).collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK).body(allPostResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong. Please try again later.");
        }
    }

}
