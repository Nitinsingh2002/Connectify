package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.PostResponseDto;
import com.Connectify.Connectify.dto.UserDto;
import com.Connectify.Connectify.entity.Follow;
import com.Connectify.Connectify.entity.Post;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.enums.FollowStatus;
import com.Connectify.Connectify.repository.IFollow;
import com.Connectify.Connectify.repository.IPost;
import com.Connectify.Connectify.repository.IUser;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExploreService {

    @Autowired
    IPost iPost;

    @Autowired
    IUser iUser;

    @Autowired
    IFollow iFollow;


    public ResponseEntity<?> searchUser(String query, UserPrinciple userDetails) {

        // Validate the query length
        if (query.length() < 3) { // Check for query length
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please enter at least 3 characters");
        }

        // Fetch all users matching the query (by username or full name)
        List<User> allResults = iUser.findByUserNameIgnoreCaseContainingOrFullNameIgnoreCaseContaining(query, query);

        // 1. Find exact matches (users with a username exactly matching the query)
        List<User> exactMatch = allResults.stream()
                .filter(user -> user.getUserName().equalsIgnoreCase(query))
                .toList();

        // 2. Find users that the current user is following
        List<Follow> followingList = iFollow.findByFollower(userDetails.getUser());

        Set<User> followingUsers = followingList.stream()
                .map(follow -> follow.getFollowing())
                .collect(Collectors.toSet());

        List<User> followingMatches = allResults.stream()
                .filter(user -> followingUsers.contains(user) && !exactMatch.contains(user))
                .toList();

        // 3. Find users who are following the current user
        List<Follow> followerList = iFollow.findByFollowing(userDetails.getUser());
        Set<User> followerUsers = followerList.stream()
                .map(follow -> follow.getFollower())
                .collect(Collectors.toSet());

        List<User> followerMatches = allResults.stream()
                .filter(user -> followerUsers.contains(user) && !exactMatch.contains(user) && !followingMatches.contains(user))
                .toList();

        // 4. Filter remaining matches (users not in exact, following, or follower matches)
        List<User> otherMatches = allResults.stream()
                .filter(user -> !exactMatch.contains(user) && !followingMatches.contains(user) && !followerMatches.contains(user))
                .toList();

        // Combine results in priority order:
        // 1. Exact matches
        // 2. Following matches
        // 3. Follower matches
        // 4. Other matches
        List<User> rankedResults = new ArrayList<>();
        rankedResults.addAll(exactMatch);
        rankedResults.addAll(followingMatches);
        rankedResults.addAll(followerMatches);
        rankedResults.addAll(otherMatches);

        // Return the ranked results in the response
        return ResponseEntity.status(HttpStatus.OK).body(rankedResults);
    }


    public ResponseEntity<?> searchPost(String query, int page, int size) {
        if (query.length() < 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please enter at least 3 characters");
        }

        //Create a pageable Object
        Pageable pageable = PageRequest.of(page, size);

        // Fetch paginated results
        Page<Post> postPage = iPost.findByCaptionIgnoreCaseContaining(query, pageable);

        if (postPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList());
        }



        //creating response of post
        List<PostResponseDto> allPost = postPage.stream().map( post -> {
            PostResponseDto response = new PostResponseDto();
            UserDto userResponse = new UserDto();

            response.setId(post.getId());
            response.setCaption(post.getCaption());
            response.setPostType(post.getPostType());
            response.setPostContentUrl(post.getPostContent());
            response.setThumbnailUrl(post.getThumbnailUrl());
            response.setCreatedAt(post.getCreatedAt());
            response.setUpdatedAt(post.getUpdatedAt());

            //we have to convert user to user dto
            userResponse.setId(post.getUser().getId());
            userResponse.setUserName(post.getUser().getUserName());
            userResponse.setFullName(post.getUser().getFullName());
            userResponse.setBio(post.getUser().getBio());
            userResponse.setEmail(post.getUser().getEmail());
            userResponse.setGender(post.getUser().getGender());

            response.setUser(userResponse);

            return response;
        }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(allPost);

    }

    public ResponseEntity<?> createTimeline(int page, int size, UserPrinciple userDetails) {

        //step 1 find all the user which is followed by current user.
        List<Follow> follows = iFollow.findByFollowerAndStatus(userDetails.getUser(), FollowStatus.ACCEPTED);
        // list of follows have all information like following and followers, but we only interested in following user because we know follower is current user
        List<User> followedUsers = follows.stream()
                .map(follow -> follow.getFollowing())
                .toList();


        //step 2 fetch the posts from followed user
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Post> followedPosts = iPost.findByUserIn(followedUsers,pageable);
        


        // Step 3: Convert followed posts to PostResponseDto
        List<PostResponseDto> allPost = followedPosts.stream().map(post -> {
            PostResponseDto postInfo = new PostResponseDto();

            // Set the basic fields of the post
            postInfo.setId(post.getId());
            postInfo.setCaption(post.getCaption());
            postInfo.setPostType(post.getPostType());
            postInfo.setCreatedAt(post.getCreatedAt());
            postInfo.setPostContentUrl(post.getPostContent());
            postInfo.setThumbnailUrl(post.getThumbnailUrl());
            postInfo.setUpdatedAt(post.getUpdatedAt());

            // Map the user details to UserDto
            UserDto userInfo = new UserDto();
            userInfo.setId(post.getUser().getId());
            userInfo.setUserName(post.getUser().getUserName());
            userInfo.setFullName(post.getUser().getFullName());
            userInfo.setBio(post.getUser().getBio());
            userInfo.setEmail(post.getUser().getEmail());
            userInfo.setGender(post.getUser().getGender());
            postInfo.setUser(userInfo);

            // Map the tagged users
           if (postInfo.getTaggedUser() != null){
               Set<UserDto> taggedUsers = postInfo.getTaggedUser().stream().map(taggedUser -> {
                   UserDto taggedUserDto = new UserDto();
                   taggedUserDto.setId(taggedUser.getId());
                   taggedUserDto.setUserName(taggedUser.getUserName());
                   taggedUserDto.setFullName(taggedUser.getFullName());
                   taggedUserDto.setBio(taggedUser.getBio());
                   taggedUserDto.setEmail(taggedUser.getEmail());
                   taggedUserDto.setGender(taggedUser.getGender());
                   return taggedUserDto;
               }).collect(Collectors.toSet());
               postInfo.setTaggedUser(taggedUsers);
           }else{
               postInfo.setTaggedUser(Collections.emptySet());
           }
            return postInfo;
        }).toList();

        if (allPost.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body("please follow more user ");
        }
        return ResponseEntity.status(HttpStatus.OK).body(allPost);
    }
}
























































































































//import React, { useState, useEffect } from "react";
//        import axios from "axios";
//
//        const InfiniteScrollPosts = ({ query }) => {
//        const [posts, setPosts] = useState([]);
//  const [page, setPage] = useState(0);
//  const [hasMore, setHasMore] = useState(true);
//  const [loading, setLoading] = useState(false);
//
//  const fetchPosts = async () => {
//        if (loading || !hasMore) return;
//
//setLoading(true);
//
//    try {
//            const response = await axios.get("/api/search/posts", {
//    params: { query, page, size: 10 },
//});
//
//        const newPosts = response.data.content;
//
//setPosts((prevPosts) => [...prevPosts, ...newPosts]);
//setHasMore(!response.data.last); // Update hasMore based on 'last' in response
//    } catch (error) {
//        console.error("Error fetching posts:", error);
//    } finally {
//setLoading(false);
//    }
//            };
//
//            const handleScroll = () => {
//        if (
//window.innerHeight + document.documentElement.scrollTop >=
//document.documentElement.offsetHeight - 100
//        ) {
//setPage((prevPage) => prevPage + 1);
//        }
//        };
//
//useEffect(() => {
//fetchPosts();
//// eslint-disable-next-line react-hooks/exhaustive-deps
//  }, [page]);
//
//useEffect(() => {
//        window.addEventListener("scroll", handleScroll);
//    return () => window.removeEventListener("scroll", handleScroll);
//  }, []);
//
//          return (
//<div>
//{posts.map((post) => (
//        <div key={post.id} className="post">
//          <h3>{post.caption}</h3>
//          <p>Created at: {new Date(post.createdAt).toLocaleString()}</p>
//        </div>
//      ))}
//
//      {loading && <p>Loading...</p>}
//        {!hasMore && <p>No more posts to load.</p>}
//    </div>
//        );
//        };
//
//export default InfiniteScrollPosts;
//


