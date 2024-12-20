package com.Connectify.Connectify.repository;

import com.Connectify.Connectify.entity.Post;
import com.Connectify.Connectify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPost extends JpaRepository<Post,Long> {
    List<Post> findAllByUser(User user);
}
