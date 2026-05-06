package com.lunabaka.javastudycard.controller;

import com.lunabaka.javastudycard.model.Question;
import com.lunabaka.javastudycard.model.QuestionType;
import com.lunabaka.javastudycard.service.QuestionBankService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MainController {

    private final QuestionBankService questionBankService;

    public MainController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", questionBankService.getCategories());
        model.addAttribute("questionTypes", questionBankService.getQuestionTypes());
        model.addAttribute("categoryStats", questionBankService.getCategoryStats());
        model.addAttribute("totalCount", questionBankService.getTotalCount());
        model.addAttribute("availableBanks", questionBankService.getAvailableBanks());
        model.addAttribute("currentBank", questionBankService.getCurrentBankName());
        return "index";
    }

    @GetMapping("/quiz")
    public String quiz(@RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "MULTIPLE_CHOICE") QuestionType type,
            @RequestParam(defaultValue = "10") int count,
            Model model) {
        List<Question> questions = questionBankService.getRandomQuestions(category, type, count);
        model.addAttribute("questions", questions);
        model.addAttribute("category", category);
        model.addAttribute("type", type);
        return "quiz";
    }

    @PostMapping("/submit")
    public String submit(HttpServletRequest request,
            @RequestParam String category,
            @RequestParam QuestionType type,
            Model model) {
        List<Map<String, Object>> results = new ArrayList<>();
        int correctCount = 0;
        int totalQuestions = 0;

        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.startsWith("question_")) {
                Long questionId = Long.parseLong(paramName.replace("question_", ""));
                Question question = questionBankService.getQuestionById(questionId).orElse(null);
                if (question == null)
                    continue;

                totalQuestions++;
                Map<String, Object> result = new HashMap<>();
                result.put("question", question);
                QuestionType qType = question.getType();

                if (qType == QuestionType.MULTIPLE_CHOICE) {
                    String[] selectedAnswers = request.getParameterValues(paramName);
                    List<Integer> selectedIds = new ArrayList<>();
                    if (selectedAnswers != null) {
                        for (String ans : selectedAnswers) {
                            selectedIds.add(Integer.parseInt(ans));
                        }
                    }
                    boolean isCorrect = isMultipleChoiceCorrect(question.getCorrectAnswers(), selectedIds);
                    result.put("isCorrect", isCorrect);
                    result.put("selectedAnswers", selectedIds);
                    if (isCorrect)
                        correctCount++;
                } else if (qType == QuestionType.SORTING) {
                    String userAnswer = request.getParameter(paramName);
                    List<Integer> userOrder = new ArrayList<>();
                    if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                        for (String part : userAnswer.split(",")) {
                            try {
                                userOrder.add(Integer.parseInt(part.trim()));
                            } catch (NumberFormatException e) {
                                // 忽略无效的数字
                            }
                        }
                    }
                    boolean isCorrect = isSortingCorrect(question.getCorrectOrder(), userOrder);
                    result.put("isCorrect", isCorrect);
                    result.put("userOrder", userOrder);
                    if (isCorrect)
                        correctCount++;
                } else {
                    String userAnswer = request.getParameter(paramName);
                    result.put("userAnswer", userAnswer);
                    result.put("isAttempted", userAnswer != null && !userAnswer.trim().isEmpty());
                    if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                        correctCount++;
                    }
                }
                results.add(result);
            }
        }

        model.addAttribute("results", results);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("category", category);
        model.addAttribute("type", type);
        return "result";
    }

    private boolean isMultipleChoiceCorrect(List<Integer> correctAnswers, List<Integer> selectedAnswers) {
        if (correctAnswers == null || selectedAnswers == null)
            return false;
        if (correctAnswers.size() != selectedAnswers.size())
            return false;
        List<Integer> sortedCorrect = new ArrayList<>(correctAnswers);
        List<Integer> sortedSelected = new ArrayList<>(selectedAnswers);
        Collections.sort(sortedCorrect);
        Collections.sort(sortedSelected);
        return sortedCorrect.equals(sortedSelected);
    }

    private boolean isSortingCorrect(List<Integer> correctOrder, List<Integer> userOrder) {
        if (correctOrder == null || userOrder == null)
            return false;
        if (correctOrder.size() != userOrder.size())
            return false;
        return correctOrder.equals(userOrder);
    }

    @GetMapping("/questions")
    public String questions(@RequestParam String category, Model model) {
        List<Question> questions = questionBankService.getQuestionsByCategory(category);
        model.addAttribute("questions", questions);
        model.addAttribute("category", category);
        return "question-list";
    }
}
