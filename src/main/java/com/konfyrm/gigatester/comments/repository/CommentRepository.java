package com.konfyrm.gigatester.comments.repository;

import com.konfyrm.gigatester.comments.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Modifying
    @Query("UPDATE Comment c SET c.user = null WHERE c.user.id = :userId")
    void anonymizeByUserId(@Param("userId") UUID userId);
}
