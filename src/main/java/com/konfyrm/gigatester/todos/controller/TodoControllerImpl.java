package com.konfyrm.gigatester.todos.controller;

import com.konfyrm.gigatester.todos.domain.dto.TodoRequest;
import com.konfyrm.gigatester.todos.domain.dto.TodoResponse;
import com.konfyrm.gigatester.todos.service.TodoService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TodoControllerImpl implements TodoController {

    private final TodoService todoService;

    @Override
    public ResponseEntity<List<TodoResponse>> list(User user) {
        return ResponseEntity.ok(todoService.list(user));
    }

    @Override
    public ResponseEntity<TodoResponse> create(User user, TodoRequest request) {
        return ResponseEntity.ok(todoService.create(user, request));
    }

    @Override
    public ResponseEntity<TodoResponse> update(User user, UUID id, TodoRequest request) {
        return ResponseEntity.ok(todoService.update(user, id, request));
    }

    @Override
    public ResponseEntity<Void> delete(User user, UUID id) {
        todoService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> clearDone(User user) {
        todoService.clearDone(user);
        return ResponseEntity.noContent().build();
    }
}
