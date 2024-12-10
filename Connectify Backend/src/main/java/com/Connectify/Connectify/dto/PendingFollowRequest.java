package com.Connectify.Connectify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PendingFollowRequest {
    private Long id;
    private String userName;
    private String fullName;
    private String bio;
}
