package com.Connectify.Connectify.controller;


import com.Connectify.Connectify.dto.PendingFollowRequest;
import com.Connectify.Connectify.entity.Follow;
import com.Connectify.Connectify.entity.UserPrinciple;
import com.Connectify.Connectify.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/user/follow")
public class FollowController {
    @Autowired
    FollowService followService;

    //api to follow user
    @PostMapping("/{userId}")
    private ResponseEntity<String> followUser(@PathVariable Long userId,
                                              @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.followUser(userId, userPrinciple);
    }

    //api to unfollow
    @DeleteMapping("/{userId}")
    private ResponseEntity<String> unfollow(@PathVariable Long userId,
                                            @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.unfollowUser(userId, userPrinciple);
    }

    //api to get all the pending following request of user
    @GetMapping("/pending-followers")
    private ResponseEntity<List<PendingFollowRequest>> getAllPendingRequest
    (@AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.getAllPendingRequest(userPrinciple);
    }

    //api to remove pending follow request
    @DeleteMapping("/pending/{userId}")
    private ResponseEntity<String> deletePendingRequest(@PathVariable Long userId,
                                                        @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.deletePendingRequest(userId, userPrinciple);
    }


    //api to accept pending follow request
    @PutMapping("/requests/{requestId}/accept")
    private ResponseEntity<String> acceptPendingRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.acceptPendingRequest(requestId, userPrinciple);
    }

    //api to get all followers
    @GetMapping("/{userId}/followers")
    private ResponseEntity<List<PendingFollowRequest>> getAllFollowers(@PathVariable Long userId,
                                                                       @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.getAllFollowers(userId, userPrinciple);
    }

    //api to get all following
    @GetMapping("/{userid}/followings")
    private ResponseEntity<List<PendingFollowRequest>> getAllFollowing(@PathVariable Long userid,
                                                                       @AuthenticationPrincipal UserPrinciple userPrinciple) {
        return followService.getAllFollowing(userid, userPrinciple);
    }


    //api to get follower count
    @GetMapping("/{userId}/followers/count")
    private ResponseEntity<Long> FollowCount(@PathVariable Long userId){
        return followService.getAllFollowersCount(userId);
    }

    @GetMapping("/{userId}/following/count")
    private ResponseEntity<Long> followingCount(@PathVariable Long userId){
        return followService.getAllFollowingCount(userId);
    }

    @GetMapping("/{userId}/mutual-followers")
    private ResponseEntity<List<PendingFollowRequest>> mutualFollower(@PathVariable Long userId,
                                                                      @AuthenticationPrincipal UserPrinciple userPrinciple){
        return followService.mutualFollowers(userId,userPrinciple);
    }


}
