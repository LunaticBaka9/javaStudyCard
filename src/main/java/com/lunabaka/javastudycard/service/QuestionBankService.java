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

    private List<Question> getAllQuestions(String bankName) {
        return bankManager.loadBank(bankName).getQuestions();
    }

    public List<String> getCategories(String bankName) {
        return getAllQuestions(bankName).stream()
                .map(Question::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<QuestionType> getQuestionTypes() {
        return Arrays.asList(QuestionType.MULTIPLE_CHOICE, QuestionType.ESSAY, QuestionType.SORTING);
    }

    public List<Question> getQuestionsByCategory(String bankName, String category) {
        if (category == null || category.equals("all")) {
            return new ArrayList<>(getAllQuestions(bankName));
        }
        return getAllQuestions(bankName).stream()
                .filter(q -> q.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByCategoryAndType(String bankName, String category, QuestionType type) {
        List<Question> questions = getQuestionsByCategory(bankName, category);
        if (type == QuestionType.MIXED) {
            return questions;
        }
        return questions.stream()
                .filter(q -> q.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByCategoryAndTypes(String bankName, String category, List<QuestionType> types) {
        List<Question> questions = getQuestionsByCategory(bankName, category);
        if (types == null || types.isEmpty()) {
            return questions;
        }
        return questions.stream()
                .filter(q -> types.contains(q.getType()))
                .collect(Collectors.toList());
    }

    public List<Question> getRandomQuestions(String bankName, String category, QuestionType type, int count) {
        List<Question> questions = getQuestionsByCategoryAndType(bankName, category, type);
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Question> getRandomQuestions(String bankName, String category, List<QuestionType> types, int count) {
        List<Question> questions = getQuestionsByCategoryAndTypes(bankName, category, types);
        Collections.shuffle(questions);
        return questions.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    public Optional<Question> getQuestionById(String bankName, Long id) {
        return getAllQuestions(bankName).stream()
                .filter(q -> q.getId().equals(id))
                .findFirst();
    }

    public int getTotalCount(String bankName) {
        return getAllQuestions(bankName).size();
    }

    public Map<String, Map<String, Integer>> getCategoryStats(String bankName) {
        Map<String, Map<String, Integer>> stats = new LinkedHashMap<>();
        List<String> categories = getCategories(bankName);
        for (String cat : categories) {
            Map<String, Integer> catStats = new LinkedHashMap<>();
            List<Question> catQuestions = getQuestionsByCategory(bankName, cat);
            catStats.put("total", catQuestions.size());
            catStats.put("multiple",
                    (int) catQuestions.stream().filter(q -> q.getType() == QuestionType.MULTIPLE_CHOICE).count());
            catStats.put("essay", (int) catQuestions.stream().filter(q -> q.getType() == QuestionType.ESSAY).count());
            catStats.put("sorting",
                    (int) catQuestions.stream().filter(q -> q.getType() == QuestionType.SORTING).count());
            stats.put(cat, catStats);
        }
        return stats;
    }

    public String getDefaultBankName() {
        return bankManager.getDefaultBankName();
    }

    public List<String> getAvailableBanks() {
        return bankManager.getAvailableBanks();
    }

    public void importBank(String name, String jsonContent) {
        bankManager.importBank(name, jsonContent);
    }
}
