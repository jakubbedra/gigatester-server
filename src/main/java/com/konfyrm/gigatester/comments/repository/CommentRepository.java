package com.konfyrm.gigatester.comments.repository;

import com.konfyrm.gigatester.comments.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
