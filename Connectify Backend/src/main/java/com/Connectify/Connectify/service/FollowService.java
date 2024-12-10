package com.Connectify.Connectify.service;


import com.Connectify.Connectify.dto.PendingFollowRequest;
import com.Connectify.Connectify.entity.Follow;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.enums.AccountType;
import com.Connectify.Connectify.enums.FollowStatus;
import com.Connectify.Connectify.repository.IFollow;
import com.Connectify.Connectify.repository.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowService {
    @Autowired
    IFollow iFollow;

    @Autowired
    IUser iUser;

    public ResponseEntity<String> followUser(Long userId, UserPrinciple userPrinciple) {

        // Check if the target user (to be followed) exists
        Optional<User> targetUser = iUser.findById(userId);
        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if the current user already follows the target user
        Optional<Follow> alreadyFollow = iFollow.findByFollowerAndFollowing(userPrinciple.getUser(), targetUser.get());
        if (alreadyFollow.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already followed");
        }

        Follow follow = new Follow();
        follow.setFollowing(targetUser.get());
        follow.setFollower(userPrinciple.getUser());

        // Set follow status based on account type
        if (targetUser.get().getAccountType().equals(AccountType.PUBLIC)) {
            follow.setStatus(FollowStatus.ACCEPTED);
        } else if (targetUser.get().getAccountType().equals(AccountType.PRIVATE)) {
            follow.setStatus(FollowStatus.PENDING);
        }

        try {
            iFollow.save(follow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("Something went wrong,please try again letter");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("User followed successfully");
    }

    public ResponseEntity<String> unfollowUser(Long userId, UserPrinciple userPrinciple) {

        // Check if the target user ( followed) exists
        Optional<User> targetUser = iUser.findById(userId);
        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The user you want to unfollow is not found");
        }

        // Check if the current user already follows the target user
        Optional<Follow> alreadyFollow = iFollow.findByFollowerAndFollowing(userPrinciple.getUser(), targetUser.get());
        if (alreadyFollow.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).
                    body("You have successfully unfollowed " + targetUser.get().getUserName());
        }

        try {
            iFollow.delete(alreadyFollow.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("Something went wrong,please try again letter");
        }

        return ResponseEntity.status(HttpStatus.OK).body("User unfollowed successfully");

    }

    public ResponseEntity<List<PendingFollowRequest>> getAllPendingRequest(UserPrinciple userPrinciple) {

        User currentUser = userPrinciple.getUser();
        List<Follow> pendingRequest = iFollow.findByFollowingAndStatus(currentUser, FollowStatus.PENDING);

        // Map the Follow entities to PendingFollowRequest DTOs
        List<PendingFollowRequest> pendingFollowRequests = pendingRequest.stream().map(follow ->
                new PendingFollowRequest(
                        follow.getFollower().getId(),
                        follow.getFollower().getUserName(),
                        follow.getFollower().getFullName(),
                        follow.getFollower().getBio()
                )
        ).toList();

        // Return the mapped DTOs
        return ResponseEntity.status(HttpStatus.OK).body(pendingFollowRequests);
    }

    public ResponseEntity<String> deletePendingRequest(Long userId, UserPrinciple userPrinciple) {
        User currentUser = userPrinciple.getUser();

        // Check if the target user (the user who sent the request) exists
        Optional<User> targetUser = iUser.findById(userId);
        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Target user not found");
        }

        // Check if there is a pending follow request where currentUser is the following and targetUser is the follower
        Optional<Follow> pendingFollowRequest = iFollow.findByFollowerAndFollowing(targetUser.get(), currentUser);
        if (pendingFollowRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pending follow request found");
        }

        // Delete the pending follow request
        try {
            iFollow.delete(pendingFollowRequest.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, please try again later");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Pending follow request deleted successfully");
    }

    public ResponseEntity<List<PendingFollowRequest>> getAllFollowers(Long userId, UserPrinciple userPrinciple) {

        Optional<User> user = iUser.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        // Check if the account is private and the current user doesn't follow the target user
        boolean isPrivateAccount = user.get().getAccountType().equals(AccountType.PRIVATE);

        Optional<Follow> checkFollowing = iFollow.findByFollowerAndFollowingAndStatus(userPrinciple.getUser(), user.get()
                , FollowStatus.ACCEPTED);

        if (isPrivateAccount && checkFollowing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }


        List<Follow> allFollowers = iFollow.findByFollowingAndStatus(user.get(), FollowStatus.ACCEPTED);

        //Map the Follow entities to PendingFollowRequest DTOs
        List<PendingFollowRequest> followers = allFollowers.stream().map(follow ->
                new PendingFollowRequest(
                        follow.getFollower().getId(),
                        follow.getFollower().getUserName(),
                        follow.getFollower().getFullName(),
                        follow.getFollower().getBio()
                )).toList();

        return ResponseEntity.status(HttpStatus.OK).body(followers);
    }

    public ResponseEntity<List<PendingFollowRequest>> getAllFollowing(Long userId, UserPrinciple userPrinciple) {
        Optional<User> user = iUser.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }


        // Check if the account is private and the current user doesn't follow the target user
        boolean isPrivateAccount = user.get().getAccountType().equals(AccountType.PRIVATE);

        Optional<Follow> checkFollowing = iFollow.findByFollowerAndFollowingAndStatus(userPrinciple.getUser(), user.get()
                , FollowStatus.ACCEPTED);

        if (isPrivateAccount && checkFollowing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }

        List<Follow> allFollowing = iFollow.findByFollowerAndStatus(user.get(), FollowStatus.ACCEPTED);

        List<PendingFollowRequest> following = allFollowing.stream().map(follow ->
                new PendingFollowRequest(
                        follow.getFollowing().getId(),
                        follow.getFollowing().getUserName(),
                        follow.getFollowing().getFullName(),
                        follow.getFollowing().getBio()
                )).toList();

        return ResponseEntity.status(HttpStatus.OK).body(following);


    }

    public ResponseEntity<String> acceptPendingRequest(Long requestId, UserPrinciple userPrinciple) {
        //get the current authentic user
        User user = userPrinciple.getUser();


        //getting follow request
        Optional<Follow> followRequest = iFollow.findByIdAndStatus(requestId, FollowStatus.PENDING);

        if (followRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No request found or request already accepted");
        }

        Follow request = followRequest.get();

        // Verify only user can accept the request for whom it made
        if (!request.getFollowing().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to accept this follow request");
        }

        request.setStatus(FollowStatus.ACCEPTED);

        try {
            iFollow.save(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong please try again latter");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Follow request accepted successfully");

    }
    
    public ResponseEntity<Long> getAllFollowersCount(Long userId) {
        Optional<User> targetUser = iUser.findById(userId);
        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0L);
        }

        Optional<Long> count = iFollow.countByFollowingAndStatus(targetUser.get(), FollowStatus.ACCEPTED);

        if (count.isEmpty()){
            return ResponseEntity.ok(0L);
        }
        return ResponseEntity.ok(count.get());
    }

    public ResponseEntity<Long> getAllFollowingCount(Long userId) {
        Optional<User> targetUser = iUser.findById(userId);
        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0L);
        }


        Optional<Long> followingCount = iFollow.countByFollowerAndStatus(targetUser.get(), FollowStatus.ACCEPTED);

        if (followingCount.isEmpty()){
            return ResponseEntity.ok(0L);
        }
        return ResponseEntity.ok(followingCount.get());
    }

    public ResponseEntity<List<PendingFollowRequest>> mutualFollowers(Long userId, UserPrinciple userPrinciple) {
        // Find the target user by ID
        Optional<User> targetUser = iUser.findById(userId);
        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        // Get the current user
        User currentUser = userPrinciple.getUser();

        // Get the list of users the current user is following
        List<Follow> currentUserFollowing = iFollow.findByFollowerAndStatus(currentUser, FollowStatus.ACCEPTED);

        // Get the list of users who are following the target user
        List<Follow> targetUserFollowers = iFollow.findByFollowingAndStatus(targetUser.get(), FollowStatus.ACCEPTED);

        // Extract mutual followers
        Set<Long> currentUserFollowingIds = currentUserFollowing.stream()
                .map(follow -> follow.getFollowing().getId())
                .collect(Collectors.toSet());

        List<PendingFollowRequest> mutualFollowers = targetUserFollowers.stream()
                .filter(follow -> currentUserFollowingIds.contains(follow.getFollower().getId()))
                .map(follow -> new PendingFollowRequest(
                        follow.getFollower().getId(),
                        follow.getFollower().getUserName(),
                        follow.getFollower().getFullName(),
                        follow.getFollower().getBio()
                ))
                .toList();

        // Return the list of mutual followers
        return ResponseEntity.status(HttpStatus.OK).body(mutualFollowers);
    }

}
