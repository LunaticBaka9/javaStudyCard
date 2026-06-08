package com.lunabaka.javastudycard.controller;

import com.lunabaka.javastudycard.model.Question;
import com.lunabaka.javastudycard.model.QuestionType;
import com.lunabaka.javastudycard.service.QuestionBankService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MainController {

    private static final String SESSION_BANK_KEY = "currentBank";

    private final QuestionBankService questionBankService;

    public MainController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    private String resolveBank(HttpSession session, String bankParam) {
        if (bankParam != null && !bankParam.isBlank()) {
            return bankParam;
        }
        String bank = (String) session.getAttribute(SESSION_BANK_KEY);
        if (bank == null) {
            bank = questionBankService.getDefaultBankName();
            session.setAttribute(SESSION_BANK_KEY, bank);
        }
        return bank;
    }

    @GetMapping("/")
    public String index(HttpSession session,
            @RequestParam(required = false) String bank,
            Model model) {
        String currentBank = resolveBank(session, bank);
        model.addAttribute("categories", questionBankService.getCategories(currentBank));
        model.addAttribute("questionTypes", questionBankService.getQuestionTypes());
        model.addAttribute("categoryStats", questionBankService.getCategoryStats(currentBank));
        model.addAttribute("totalCount", questionBankService.getTotalCount(currentBank));
        model.addAttribute("availableBanks", questionBankService.getAvailableBanks());
        model.addAttribute("currentBank", currentBank);
        return "index";
    }

    @GetMapping("/quiz")
    public String quiz(HttpSession session,
            @RequestParam(required = false) String bank,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "MULTIPLE_CHOICE") List<QuestionType> types,
            @RequestParam(defaultValue = "10") int count,
            Model model) {
        String currentBank = resolveBank(session, bank);
        List<Question> questions = questionBankService.getRandomQuestions(currentBank, category, types, count);
        model.addAttribute("questions", questions);
        model.addAttribute("category", category);
        model.addAttribute("types", types);
        model.addAttribute("typeDisplay", buildTypeDisplay(types));
        model.addAttribute("currentBank", currentBank);
        return "quiz";
    }

    private String buildTypeDisplay(List<QuestionType> types) {
        if (types == null || types.isEmpty()) {
            return "综合练习";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            QuestionType t = types.get(i);
            switch (t) {
                case MULTIPLE_CHOICE:
                    sb.append("多选题");
                    break;
                case ESSAY:
                    sb.append("问答题");
                    break;
                case SORTING:
                    sb.append("排序题");
                    break;
                default:
                    sb.append(t.name());
            }
        }
        return sb.toString();
    }

    @PostMapping("/submit")
    public String submit(HttpServletRequest request, HttpSession session,
            @RequestParam(required = false) String bank,
            @RequestParam String category,
            @RequestParam(required = false) String types,
            @RequestParam(required = false, defaultValue = "0") int totalQuestions,
            Model model) {
        String currentBank = resolveBank(session, bank);
        List<Map<String, Object>> results = new ArrayList<>();
        int correctCount = 0;
        int processedQuestions = 0;

        Set<Long> processedQuestionIds = new HashSet<>();

        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (!paramName.startsWith("question_")) {
                continue;
            }

            String paramValue = request.getParameter(paramName);
            Long questionId = Long.parseLong(paramName.replace("question_", ""));
            if (processedQuestionIds.contains(questionId)) {
                continue;
            }
            processedQuestionIds.add(questionId);

            Question question = questionBankService.getQuestionById(currentBank, questionId).orElse(null);
            if (question == null)
                continue;

            processedQuestions++;
            Map<String, Object> result = new HashMap<>();
            result.put("question", question);
            QuestionType qType = question.getType();

            if (qType == QuestionType.MULTIPLE_CHOICE) {
                String[] selectedAnswers = request.getParameterValues("answer_" + questionId);
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
                String userAnswer = paramValue;
                List<Integer> userOrder = new ArrayList<>();
                if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                    for (String part : userAnswer.split(",")) {
                        try {
                            userOrder.add(Integer.parseInt(part.trim()));
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                boolean isCorrect = isSortingCorrect(question.getCorrectOrder(), userOrder);
                result.put("isCorrect", isCorrect);
                result.put("userOrder", userOrder);
                if (isCorrect)
                    correctCount++;
            } else {
                String userAnswer = paramValue;
                result.put("userAnswer", userAnswer);
                result.put("isAttempted", userAnswer != null && !userAnswer.trim().isEmpty());
                if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                    correctCount++;
                }
            }
            results.add(result);
        }

        int actualTotal = totalQuestions > 0 ? totalQuestions : processedQuestions;

        model.addAttribute("results", results);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", actualTotal);
        model.addAttribute("originalCount", totalQuestions);
        model.addAttribute("category", category);
        model.addAttribute("types", types);
        model.addAttribute("typeDisplay", buildTypeDisplayFromString(types));
        model.addAttribute("currentBank", currentBank);
        return "result";
    }

    private String buildTypeDisplayFromString(String types) {
        if (types == null || types.isEmpty()) {
            return "综合练习";
        }
        StringBuilder sb = new StringBuilder();
        String[] typeArray = types.split(",");
        for (int i = 0; i < typeArray.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            try {
                QuestionType t = QuestionType.valueOf(typeArray[i].trim());
                switch (t) {
                    case MULTIPLE_CHOICE:
                        sb.append("多选题");
                        break;
                    case ESSAY:
                        sb.append("问答题");
                        break;
                    case SORTING:
                        sb.append("排序题");
                        break;
                    default:
                        sb.append(t.name());
                }
            } catch (IllegalArgumentException e) {
                sb.append(typeArray[i].trim());
            }
        }
        return sb.toString();
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
    public String questions(HttpSession session,
            @RequestParam(required = false) String bank,
            @RequestParam String category, Model model) {
        String currentBank = resolveBank(session, bank);
        List<Question> questions = questionBankService.getQuestionsByCategory(currentBank, category);
        model.addAttribute("questions", questions);
        model.addAttribute("category", category);
        model.addAttribute("currentBank", currentBank);
        return "question-list";
    }
}
