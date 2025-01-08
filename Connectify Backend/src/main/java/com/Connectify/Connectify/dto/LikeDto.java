package com.Connectify.Connectify.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeDto {
    private Long id;

    private String userName;

    private Long userId;

    private String userProfilePicture;

}
