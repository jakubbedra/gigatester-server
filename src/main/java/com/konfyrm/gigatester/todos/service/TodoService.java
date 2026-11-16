package com.konfyrm.gigatester.todos.service;

import com.konfyrm.gigatester.articles.repository.ArticleRepository;
import com.konfyrm.gigatester.coding.repository.CodingTaskRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.features.domain.FeatureFlag;
import com.konfyrm.gigatester.features.service.FeatureFlagService;
import com.konfyrm.gigatester.graphs.repository.GraphRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.todos.domain.TodoLinkType;
import com.konfyrm.gigatester.todos.domain.TodoStatus;
import com.konfyrm.gigatester.todos.domain.dto.TodoRequest;
import com.konfyrm.gigatester.todos.domain.dto.TodoResponse;
import com.konfyrm.gigatester.todos.domain.entity.TodoItem;
import com.konfyrm.gigatester.todos.repository.TodoItemRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** The calling user's private to-do list; every operation is scoped to that user. */
@Service
@RequiredArgsConstructor
public class TodoService {

    private static final int MAX_TEXT_LENGTH = 500;

    private final TodoItemRepository todoRepository;
    private final TestRepository testRepository;
    private final CrosswordRepository crosswordRepository;
    private final GraphRepository graphRepository;
    private final CodingTaskRepository codingTaskRepository;
    private final ArticleRepository articleRepository;
    private final SubjectGroupAccessService accessService;
    private final FeatureFlagService featureFlags;

    public List<TodoResponse> list(User user) {
        featureFlags.require(FeatureFlag.TODO_LIST);
        return todoRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .sorted(Comparator.comparing((TodoItem t) -> t.effectiveStatus() == TodoStatus.DONE)
                        .thenComparing(TodoItem::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TodoItem::getCreatedAt, Comparator.reverseOrder()))
                .map(item -> toResponse(item, user))
                .toList();
    }

    public TodoResponse create(User user, TodoRequest request) {
        featureFlags.require(FeatureFlag.TODO_LIST);
        TodoItem item = TodoItem.builder().user(user).build();
        apply(item, request, user);
        return toResponse(todoRepository.save(item), user);
    }

    public TodoResponse update(User user, UUID id, TodoRequest request) {
        featureFlags.require(FeatureFlag.TODO_LIST);
        TodoItem item = owned(user, id);
        apply(item, request, user);
        return toResponse(todoRepository.save(item), user);
    }

    public void delete(User user, UUID id) {
        featureFlags.require(FeatureFlag.TODO_LIST);
        todoRepository.delete(owned(user, id));
    }

    @Transactional
    public void clearDone(User user) {
        featureFlags.require(FeatureFlag.TODO_LIST);
        todoRepository.deleteByUser_IdAndDoneTrue(user.getId());
    }

    private TodoItem owned(User user, UUID id) {
        return todoRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(TodoItem item, TodoRequest request, User user) {
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is required");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text too long");
        }
        item.setText(text);
        if (request.getStatus() != null) {
            item.applyStatus(request.getStatus());
        } else if (request.getDone() != null) {
            item.applyStatus(request.getDone() ? TodoStatus.DONE : TodoStatus.TODO);
        } else if (item.getStatus() == null) {
            item.applyStatus(TodoStatus.TODO);
        }
        item.setDueDate(request.getDueDate());

        if (request.getLinkType() != null && request.getLinkId() != null) {
            // Only entities the user can actually open may be attached.
            if (linkName(request.getLinkType(), request.getLinkId(), user) == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot link to this item");
            }
            item.setLinkType(request.getLinkType());
            item.setLinkId(request.getLinkId());
        } else {
            item.setLinkType(null);
            item.setLinkId(null);
        }
    }

    private TodoResponse toResponse(TodoItem item, User user) {
        String linkName = item.getLinkType() == null ? null : linkName(item.getLinkType(), item.getLinkId(), user);
        return TodoResponse.builder()
                .id(item.getId())
                .text(item.getText())
                .done(item.effectiveStatus() == TodoStatus.DONE)
                .status(item.effectiveStatus())
                .dueDate(item.getDueDate())
                .linkType(item.getLinkType())
                .linkId(item.getLinkId())
                .linkName(linkName)
                .createdAt(item.getCreatedAt())
                .build();
    }

    /** Name of the target if it exists and the user may open it, otherwise null. */
    private String linkName(TodoLinkType type, UUID id, User user) {
        return switch (type) {
            case TEST -> accessService.hasAccessToTest(id, user)
                    ? testRepository.findById(id).map(t -> t.getName()).orElse(null) : null;
            case CROSSWORD -> accessService.hasAccessToCrossword(id, user)
                    ? crosswordRepository.findById(id).map(c -> c.getName()).orElse(null) : null;
            case GRAPH -> accessService.hasAccessToGraph(id, user)
                    ? graphRepository.findById(id).map(g -> g.getName()).orElse(null) : null;
            case CODING_TASK -> accessService.hasAccessToCodingTask(id, user)
                    ? codingTaskRepository.findById(id).map(c -> c.getName()).orElse(null) : null;
            case ARTICLE -> accessService.hasAccessToArticle(id, user)
                    ? articleRepository.findById(id).map(a -> a.getName()).orElse(null) : null;
        };
    }
}
