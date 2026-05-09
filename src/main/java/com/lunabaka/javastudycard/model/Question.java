package com.lunabaka.javastudycard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    /**
     * 获取HTML格式的答案，将换行符替换为<br/>
     * 标签
     */
    public String getAnswerHtml() {
        if (answer == null) {
            return null;
        }
        return answer.replace("\n", "<br/>");
    }
}
