package com.exam.system.model;

import java.io.Serializable;

/**
 * Abstract Question class demonstrating Abstraction and Polymorphism.
 * Specific question types (MCQQuestion, TrueFalseQuestion) inherit and implement
 * specialized evaluation and display logic.
 */
public abstract class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String questionText;
    private int marks;

    public Question(int id, String questionText, int marks) {
        this.id = id;
        this.questionText = questionText;
        this.marks = marks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    // Abstract methods specialized via Polymorphism
    public abstract String getQuestionType();

    public abstract void displayQuestion();

    public abstract boolean evaluateAnswer(String studentAnswer);

    public abstract String getCorrectAnswerDisplay();
}
