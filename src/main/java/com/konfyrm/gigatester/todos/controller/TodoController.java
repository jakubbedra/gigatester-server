package com.konfyrm.gigatester.todos.controller;

import com.konfyrm.gigatester.todos.domain.dto.TodoRequest;
import com.konfyrm.gigatester.todos.domain.dto.TodoResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/todos")
public interface TodoController {

    @GetMapping
    ResponseEntity<List<TodoResponse>> list(@AuthenticationPrincipal User user);

    @PostMapping
    ResponseEntity<TodoResponse> create(@AuthenticationPrincipal User user, @RequestBody TodoRequest request);

    @PutMapping("/{id}")
    ResponseEntity<TodoResponse> update(@AuthenticationPrincipal User user, @PathVariable UUID id, @RequestBody TodoRequest request);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id);

    @DeleteMapping("/done")
    ResponseEntity<Void> clearDone(@AuthenticationPrincipal User user);
}
