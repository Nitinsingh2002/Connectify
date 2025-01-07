package com.Connectify.Connectify.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 1, message = "Comment must be at least 1 character long")
    @Size(max = 400, message = "Comment must not exceed 400 characters")
    private String text;

    @NotNull(message = "Post ID is required")
    private Long postId;


}
