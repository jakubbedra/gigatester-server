package com.konfyrm.gigatester.questions.repository;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query(value = """
        SELECT q.*
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
        ORDER BY RANDOM()
        LIMIT :count
    """, nativeQuery = true)
    List<Question> findRandomQuestions(
            @Param("testId") UUID testId,
            @Param("type") String type,
            @Param("count") int count
    );

    @Query(value = """
        SELECT q.*
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
        ORDER BY RANDOM()
    """, nativeQuery = true)
    List<Question> findRandomQuestions(
            @Param("testId") UUID testId,
            @Param("type") String type
    );

    @Query(value = """
        SELECT q.*
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        JOIN questions_tags qt ON qt.question_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
            AND qt.tag_id IN (:tagIds)
        ORDER BY RANDOM()
        LIMIT :count
    """, nativeQuery = true)
    List<Question> findRandomQuestionsByTags(
            @Param("testId") UUID testId,
            @Param("type") String type,
            @Param("count") int count,
            @Param("tagIds") Collection<UUID> tagIds
    );

    @Query(value = """
        SELECT q.*
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        JOIN questions_tags qt ON qt.question_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
            AND qt.tag_id IN (:tagIds)
        ORDER BY RANDOM()
    """, nativeQuery = true)
    List<Question> findRandomQuestionsByTags(
            @Param("testId") UUID testId,
            @Param("type") String type,
            @Param("tagIds") Collection<UUID> tagIds
    );

    @Query(value = """
        SELECT COUNT(DISTINCT q.id)
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
    """, nativeQuery = true)
    long countByTestIdAndType(@Param("testId") UUID testId, @Param("type") String type);

    @Query(value = """
        SELECT COUNT(DISTINCT q.id)
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        JOIN questions_tags qt ON qt.question_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
            AND qt.tag_id IN (:tagIds)
    """, nativeQuery = true)
    long countByTestIdAndTypeAndTags(
            @Param("testId") UUID testId,
            @Param("type") String type,
            @Param("tagIds") Collection<UUID> tagIds
    );

    @Query(value = """
        SELECT q.*
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        WHERE tq.test_id = :testId
    """, nativeQuery = true)
    List<Question> findAllByTestId(@Param("testId") UUID testId);

    @Query(value = """
        SELECT q.*
        FROM questions q
        JOIN test_templates_questions tq ON tq.questions_id = q.id
        WHERE tq.test_id = :testId
            AND q.type = :type
    """, nativeQuery = true)
    List<Question> findAllByTestIdAndType(@Param("testId") UUID testId, @Param("type") String type);

}