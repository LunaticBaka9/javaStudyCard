package com.lunabaka.javastudycard.service;

import com.lunabaka.javastudycard.model.Question;
import com.lunabaka.javastudycard.model.QuestionType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private final QuestionBankManager bankManager;

    public QuestionBankService(QuestionBankManager bankManager) {
        this.bankManager = bankManager;
    }

    private List<Question> getAllQuestions() {
        return bankManager.getCurrentBank().getQuestions();
    }

    public List<String> getCategories() {
        return getAllQuestions().stream()
                .map(Question::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<QuestionType> getQuestionTypes() {
        return Arrays.asList(QuestionType.MULTIPLE_CHOICE, QuestionType.ESSAY, QuestionType.SORTING);
    }

    public List<Question> getQuestionsByCategory(String category) {
        if (category == null || category.equals("all")) {
            return new ArrayList<>(getAllQuestions());
        }
        return getAllQuestions().stream()
                .filter(q -> q.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByCategoryAndType(String category, QuestionType type) {
        List<Question> questions = getQuestionsByCategory(category);
        if (type == QuestionType.MIXED) {
            return questions;
        }
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
        return getAllQuestions().stream()
                .filter(q -> q.getId().equals(id))
                .findFirst();
    }

    public int getTotalCount() {
        return getAllQuestions().size();
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
            catStats.put("sorting", (int) catQuestions.stream().filter(q -> q.getType() == QuestionType.SORTING).count());
            stats.put(cat, catStats);
        }
        return stats;
    }

    public String getCurrentBankName() {
        return bankManager.getCurrentBankName();
    }

    public List<String> getAvailableBanks() {
        return bankManager.getAvailableBanks();
    }

    public void switchBank(String bankName) {
        bankManager.setCurrentBank(bankName);
    }

    public void importBank(String name, String jsonContent) {
        bankManager.importBank(name, jsonContent);
    }
}
