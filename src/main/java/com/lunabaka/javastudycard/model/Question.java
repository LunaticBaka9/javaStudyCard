package com.lunabaka.javastudycard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Long id;
    private String category;
    private QuestionType type;
    private String question;
    private List<String> options;
    private List<Integer> correctAnswers;
    private String answer;
    // 排序题目专用字段
    private List<String> items; // 待排序的项
    private List<Integer> correctOrder; // 正确的顺序（索引数组）
}
