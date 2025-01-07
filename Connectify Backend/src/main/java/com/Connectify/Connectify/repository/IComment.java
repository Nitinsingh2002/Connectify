package com.Connectify.Connectify.repository;

import com.Connectify.Connectify.entity.Comment;
import com.Connectify.Connectify.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IComment extends JpaRepository<Comment,Long>{


    List<Comment> findAllByParentComment(Comment comment);

    List<Comment> findAllByParentCommentAndPost(Comment parentComment, Post post);
}
