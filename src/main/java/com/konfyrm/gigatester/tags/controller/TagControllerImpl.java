package com.konfyrm.gigatester.tags.controller;

import com.konfyrm.gigatester.tags.dto.TagRequest;
import com.konfyrm.gigatester.tags.dto.TagResponse;
import com.konfyrm.gigatester.tags.entity.Tag;
import com.konfyrm.gigatester.tags.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TagControllerImpl implements TagController {

    private final TagService tagService;

    @Autowired
    public TagControllerImpl(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public ResponseEntity<?> createTag(TagRequest request) {
        Tag saved = tagService.save(Tag.builder().key(request.getKey()).build());
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getTags() {
        List<TagResponse> tags = tagService.findAll().stream()
                .map(t -> TagResponse.builder().id(t.getId()).key(t.getKey()).build())
                .toList();
        return ResponseEntity.ok(tags);
    }

    @Override
    public ResponseEntity<?> getTag(UUID id) {
        Tag tag = tagService.findById(id);
        return ResponseEntity.ok(TagResponse.builder().id(tag.getId()).key(tag.getKey()).build());
    }

    @Override
    public ResponseEntity<?> updateTag(UUID id, TagRequest request) {
        Tag tag = tagService.findById(id);
        tag.setKey(request.getKey());
        tagService.save(tag);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> deleteTag(UUID id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
