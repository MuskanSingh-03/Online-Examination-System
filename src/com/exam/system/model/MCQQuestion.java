package com.exam.system.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multiple Choice Question (MCQ) subclass specializing Question.
 * Demonstrates inheritance and method overriding for polymorphic evaluation.
 */
public class MCQQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private List<String> options;
    private int correctOptionIndex; // 1-based index (1, 2, 3, or 4)

    public MCQQuestion(int id, String questionText, int marks, List<String> options, int correctOptionIndex) {
        super(id, questionText, marks);
        this.options = new ArrayList<>(options);
        this.correctOptionIndex = correctOptionIndex;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    @Override
    public String getQuestionType() {
        return "MCQ";
    }

    @Override
    public void displayQuestion() {
        System.out.println("\n[Q" + getId() + "] (" + getQuestionType() + " | " + getMarks() + " Marks)");
        System.out.println(getQuestionText());
        for (int i = 0; i < options.size(); i++) {
            System.out.println("   (" + (i + 1) + ") " + options.get(i));
        }
    }

    @Override
    public boolean evaluateAnswer(String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }
        try {
            int selected = Integer.parseInt(studentAnswer.trim());
            return selected == this.correctOptionIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getCorrectAnswerDisplay() {
        if (correctOptionIndex >= 1 && correctOptionIndex <= options.size()) {
            return "Option (" + correctOptionIndex + ") - " + options.get(correctOptionIndex - 1);
        }
        return "Option (" + correctOptionIndex + ")";
    }
}
