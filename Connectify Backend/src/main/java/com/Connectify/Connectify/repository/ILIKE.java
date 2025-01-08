package com.Connectify.Connectify.repository;


import com.Connectify.Connectify.entity.Comment;
import com.Connectify.Connectify.entity.Like;
import com.Connectify.Connectify.entity.Post;
import com.Connectify.Connectify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILIKE  extends JpaRepository<Like,Long> {

    Optional<Like> findByPostAndUser(Post postId, User id);

    Optional<Like> findByCommentAndUser(Comment comment, User user);

    List<Like> findByPost(Post id);

    List<Like> findByComment(Comment comment);
}
