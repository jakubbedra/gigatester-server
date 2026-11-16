package com.konfyrm.gigatester.todos.repository;

import com.konfyrm.gigatester.todos.domain.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoItemRepository extends JpaRepository<TodoItem, UUID> {
    List<TodoItem> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<TodoItem> findByIdAndUser_Id(UUID id, UUID userId);

    void deleteByUser_IdAndDoneTrue(UUID userId);
}
