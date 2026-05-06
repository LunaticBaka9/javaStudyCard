package com.lunabaka.javastudycard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunabaka.javastudycard.model.Question;
import com.lunabaka.javastudycard.model.QuestionBank;
import com.lunabaka.javastudycard.model.QuestionType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private List<Question> allQuestions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data/card.json");
            try (InputStream is = resource.getInputStream()) {
                QuestionBank questionBank = objectMapper.readValue(is, QuestionBank.class);
                allQuestions = questionBank.getQuestions();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load question bank", e);
        }
    }

    public List<String> getCategories() {
        return allQuestions.stream()
                .map(Question::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<QuestionType> getQuestionTypes() {
        return Arrays.asList(QuestionType.MULTIPLE_CHOICE, QuestionType.ESSAY);
    }

    public List<Question> getQuestionsByCategory(String category) {
        if (category == null || category.equals("all")) {
            return new ArrayList<>(allQuestions);
        }
        return allQuestions.stream()
                .filter(q -> q.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByCategoryAndType(String category, QuestionType type) {
        List<Question> questions = getQuestionsByCategory(category);
        return questions.stream()
                .filter(q -> q.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Question> getRandomQuestions(String category, QuestionType type, int count) {
        List<Question> questions = getQuestionsByCategoryAndType(category, type);
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    public Optional<Question> getQuestionById(Long id) {
        return allQuestions.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst();
    }

    public int getTotalCount() {
        return allQuestions.size();
    }

    public Map<String, Map<String, Integer>> getCategoryStats() {
        Map<String, Map<String, Integer>> stats = new LinkedHashMap<>();
        List<String> categories = getCategories();
        for (String cat : categories) {
            Map<String, Integer> catStats = new LinkedHashMap<>();
            List<Question> catQuestions = getQuestionsByCategory(cat);
            catStats.put("total", catQuestions.size());
            catStats.put("multiple", (int) catQuestions.stream().filter(q -> q.getType() == QuestionType.MULTIPLE_CHOICE).count());
            catStats.put("essay", (int) catQuestions.stream().filter(q -> q.getType() == QuestionType.ESSAY).count());
            stats.put(cat, catStats);
        }
        return stats;
    }
}
