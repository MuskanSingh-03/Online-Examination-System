package com.exam.system.model;

/**
 * True/False Question subclass specializing Question.
 * Demonstrates polymorphic evaluation logic customized for boolean responses.
 */
public class TrueFalseQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private boolean correctAnswer;

    public TrueFalseQuestion(int id, String questionText, int marks, boolean correctAnswer) {
        super(id, questionText, marks);
        this.correctAnswer = correctAnswer;
    }

    public boolean isCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String getQuestionType() {
        return "True/False";
    }

    @Override
    public void displayQuestion() {
        System.out.println("\n[Q" + getId() + "] (" + getQuestionType() + " | " + getMarks() + " Marks)");
        System.out.println(getQuestionText());
        System.out.println("   (1) True / (T)");
        System.out.println("   (2) False / (F)");
    }

    @Override
    public boolean evaluateAnswer(String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }
        String clean = studentAnswer.trim().toUpperCase();
        boolean studentBool;
        if (clean.equals("1") || clean.equals("T") || clean.equals("TRUE")) {
            studentBool = true;
        } else if (clean.equals("2") || clean.equals("F") || clean.equals("FALSE")) {
            studentBool = false;
        } else {
            return false;
        }
        return studentBool == this.correctAnswer;
    }

    @Override
    public String getCorrectAnswerDisplay() {
        return correctAnswer ? "True (Option 1)" : "False (Option 2)";
    }
}
