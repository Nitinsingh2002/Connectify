package com.Connectify.Connectify.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDto {

    private  Long id;

    private  String text;

    private LocalDateTime createdAt;

    private  Long UserId;

    private  String userName;

    private  String UserprofilePicture;

    private  Long parentCommentId;
}
