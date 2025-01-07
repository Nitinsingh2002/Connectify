package com.Connectify.Connectify.entity;

import com.Connectify.Connectify.enums.LikeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "Likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;


    @Enumerated(EnumType.STRING)
    private LikeType likeType;


    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private  User user;


    @ManyToOne
    @JoinColumn(name = "post_id")
    private  Post post;


    @ManyToOne
    @JoinColumn(name = "Comment_id")
    private  Comment comment;

}


//one user have multiple like (different  post)
//one post have multiple like

//we can create bidirectional mapping for like for good performance
//if we want to add a feat : like on comment we can create an enum type post comment