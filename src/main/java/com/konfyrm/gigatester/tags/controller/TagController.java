package com.konfyrm.gigatester.tags.controller;

import com.konfyrm.gigatester.tags.dto.TagRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface TagController {

    @PostMapping("api/v1/tags")
    ResponseEntity<?> createTag(@RequestBody TagRequest request);

    @GetMapping("api/v1/tags")
    ResponseEntity<?> getTags();

    @GetMapping("api/v1/tags/{id}")
    ResponseEntity<?> getTag(@PathVariable UUID id);

    @PutMapping("api/v1/tags/{id}")
    ResponseEntity<?> updateTag(@PathVariable UUID id, @RequestBody TagRequest request);

    @DeleteMapping("api/v1/tags/{id}")
    ResponseEntity<?> deleteTag(@PathVariable UUID id);

}
