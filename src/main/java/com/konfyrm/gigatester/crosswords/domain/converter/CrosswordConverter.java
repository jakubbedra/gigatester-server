package com.konfyrm.gigatester.crosswords.domain.converter;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordTermResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordsResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.tags.dto.TagResponse;
import com.konfyrm.gigatester.tags.entity.Tag;
import com.konfyrm.gigatester.tags.repository.TagRepository;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CrosswordConverter {

    private final TagRepository tagRepository;

    @Autowired
    public CrosswordConverter(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Crossword toEntity(CrosswordRequest request) {
        List<CrosswordTerm> terms = request.getTerms() == null ? List.of() :
                request.getTerms().stream()
                        .map(t -> {
                            List<Tag> tags = resolveTags(t.getTagIds());
                            return CrosswordTerm.builder()
                                    .term(t.getTerm())
                                    .clue(t.getClue())
                                    .clueType(t.getClueType())
                                    .tags(tags)
                                    .build();
                        })
                        .toList();

        return Crossword.builder()
                .name(request.getName())
                .terms(terms)
                .build();
    }

    public CrosswordResponse toResponse(Crossword crossword) {
        List<CrosswordTermResponse> terms = crossword.getTerms() == null ? List.of() :
                crossword.getTerms().stream()
                        .map(t -> CrosswordTermResponse.builder()
                                .id(t.getId())
                                .term(t.getTerm())
                                .clue(t.getClue())
                                .clueType(t.getClueType())
                                .tags(toTagResponses(t.getTags()))
                                .build())
                        .toList();

        List<UserResponse> authors = crossword.getAuthors() == null ? List.of() :
                crossword.getAuthors().stream()
                        .map(u -> UserResponse.builder().id(u.getId()).username(u.getUsername())
                                .role(u.getRole()).profilePictureUrl(u.getProfilePictureUrl()).build())
                        .toList();

        return CrosswordResponse.builder()
                .id(crossword.getId())
                .name(crossword.getName())
                .terms(terms)
                .authors(authors)
                .build();
    }

    public CrosswordsResponse toResponse(List<Crossword> crosswords) {
        return new CrosswordsResponse(crosswords.stream()
                .map(c -> CrosswordsResponse.CrosswordSummaryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .termCount(c.getTerms() == null ? 0 : c.getTerms().size())
                        .build())
                .toList());
    }

    private List<Tag> resolveTags(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(tagRepository.findAllById(tagIds));
    }

    private List<TagResponse> toTagResponses(List<Tag> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .map(tag -> TagResponse.builder().id(tag.getId()).key(tag.getKey()).build())
                .toList();
    }
}
