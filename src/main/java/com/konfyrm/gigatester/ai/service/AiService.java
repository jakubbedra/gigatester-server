package com.konfyrm.gigatester.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konfyrm.gigatester.ai.dto.AiAnswerDto;
import com.konfyrm.gigatester.ai.dto.AiQuestionDto;
import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.entity.*;
import com.konfyrm.gigatester.questions.domain.entity.enums.ContentType;
import com.konfyrm.gigatester.questions.service.QuestionService;
import com.konfyrm.gigatester.tags.entity.Tag;
import com.konfyrm.gigatester.tags.repository.TagRepository;
import com.konfyrm.gigatester.tests.service.TestService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class AiService {

    private static final String AI_TAG_KEY = "ai-generated";
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final TagRepository tagRepository;
    private final QuestionService questionService;
    private final TestService testService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public AiService(TagRepository tagRepository, QuestionService questionService,
                     TestService testService, ObjectMapper objectMapper) {
        this.tagRepository = tagRepository;
        this.questionService = questionService;
        this.testService = testService;
        this.objectMapper = objectMapper;
    }

    public List<AiQuestionDto> generateQuestions(MultipartFile pdf,
                                                  int closedCount,
                                                  int multipleChoiceCount,
                                                  int openCount) throws IOException {
        String text = extractText(pdf);
        String prompt = buildPrompt(text, closedCount, multipleChoiceCount, openCount);
        String json = callOpenAi(prompt);
        return parseResponse(json);
    }

    public void saveQuestions(UUID testId, List<AiQuestionDto> dtos) {
        Tag aiTag = tagRepository.findByKey(AI_TAG_KEY)
                .orElseGet(() -> tagRepository.save(Tag.builder().key(AI_TAG_KEY).build()));

        List<com.konfyrm.gigatester.questions.domain.entity.Question> saved = new ArrayList<>();
        for (AiQuestionDto dto : dtos) {
            saved.add(toEntity(dto, aiTag));
        }
        testService.addQuestionsToTest(testId, saved);
    }

    private String extractText(MultipartFile pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String buildPrompt(String text, int closed, int multipleChoice, int open) {
        return String.format("""
                You are an exam question generator. Based on the text provided, generate exam questions.

                Generate exactly:
                - %d single-answer closed questions (exactly one correct answer among choices)
                - %d multiple-answer closed questions (one or more correct answers)
                - %d open-ended questions (free text answer)

                Return ONLY a JSON object in this exact format:
                {
                  "questions": [
                    {
                      "type": "CLOSED_QUESTION",
                      "multipleChoice": false,
                      "questionText": "...",
                      "answers": [
                        {"text": "...", "correct": true},
                        {"text": "...", "correct": false},
                        {"text": "...", "correct": false},
                        {"text": "...", "correct": false}
                      ],
                      "openAnswer": null
                    },
                    {
                      "type": "OPEN_QUESTION",
                      "multipleChoice": false,
                      "questionText": "...",
                      "answers": [],
                      "openAnswer": "expected answer text"
                    }
                  ]
                }

                Rules:
                - Single-answer closed questions: multipleChoice=false, exactly one answer has correct=true
                - Multiple-answer closed questions: multipleChoice=true, one or more answers have correct=true
                - Open questions: type=OPEN_QUESTION, answers=[], openAnswer has the expected answer
                - Generate 4 answer options for each closed question
                - All text must be in the same language as the source text

                Source text:
                %s
                """, closed, multipleChoice, open, text);
    }

    private String callOpenAi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(message),
                "response_format", Map.of("type", "json_object")
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }

    private List<AiQuestionDto> parseResponse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode questionsNode = root.path("questions");
        List<AiQuestionDto> result = new ArrayList<>();
        for (JsonNode q : questionsNode) {
            AiQuestionDto dto = new AiQuestionDto();
            dto.setType(q.path("type").asText("CLOSED_QUESTION"));
            dto.setMultipleChoice(q.path("multipleChoice").asBoolean(false));
            dto.setQuestionText(q.path("questionText").asText());
            dto.setOpenAnswer(q.path("openAnswer").isNull() ? null : q.path("openAnswer").asText());
            List<AiAnswerDto> answers = new ArrayList<>();
            for (JsonNode a : q.path("answers")) {
                answers.add(new AiAnswerDto(a.path("text").asText(), a.path("correct").asBoolean()));
            }
            dto.setAnswers(answers);
            result.add(dto);
        }
        return result;
    }

    private com.konfyrm.gigatester.questions.domain.entity.Question toEntity(AiQuestionDto dto, Tag aiTag) {
        QuestionContent content = QuestionContent.builder()
                .text(dto.getQuestionText())
                .type(ContentType.TEXT)
                .build();

        if ("OPEN_QUESTION".equals(dto.getType())) {
            QuestionContent answer = QuestionContent.builder()
                    .text(dto.getOpenAnswer() != null ? dto.getOpenAnswer() : "")
                    .type(ContentType.TEXT)
                    .build();
            OpenQuestion q = OpenQuestion.builder()
                    .type(TesterEntityType.OPEN_QUESTION)
                    .content(content)
                    .answer(answer)
                    .tags(new ArrayList<>(List.of(aiTag)))
                    .build();
            return questionService.saveQuestion(q);
        } else {
            List<ClosedQuestionAnswer> answers = dto.getAnswers().stream()
                    .map(a -> ClosedQuestionAnswer.builder()
                            .text(a.getText())
                            .correct(a.isCorrect())
                            .build())
                    .toList();
            ClosedQuestion q = ClosedQuestion.builder()
                    .type(TesterEntityType.CLOSED_QUESTION)
                    .content(content)
                    .multipleChoice(dto.isMultipleChoice())
                    .answers(answers)
                    .tags(new ArrayList<>(List.of(aiTag)))
                    .build();
            return questionService.saveQuestion(q);
        }
    }
}
