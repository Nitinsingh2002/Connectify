package com.Connectify.Connectify.repository;

import com.Connectify.Connectify.entity.Post;
import com.Connectify.Connectify.entity.User;
import com.Connectify.Connectify.enums.AccountType;
import com.Connectify.Connectify.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPost extends JpaRepository<Post,Long> {
    List<Post> findAllByUser(User user);

    Page<Post> findByCaptionIgnoreCaseContaining(String query, Pageable pageable);

    Page<Post> findByUserIn(List<User> followedUser, Pageable pageable);

    List<Post> findByPostTypeAndUser_AccountType(PostType postType, AccountType accountType, Pageable pageable);

    List<Post> findByPostType(PostType postType,  Pageable pageable);
}
