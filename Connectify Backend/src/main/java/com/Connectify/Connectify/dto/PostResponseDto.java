package com.Connectify.Connectify.dto;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.enums.PostType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PostResponseDto {

    private Long id;

    private String caption;

    private PostType postType;

    private String postContentUrl;

    private String thumbnailUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UserDto user;

    private Set<UserDto> taggedUser;
}
