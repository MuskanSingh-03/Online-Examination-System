package com.exam.system.model;

/** Short response question with case and whitespace insensitive automatic marking. */
public class ShortAnswerQuestion extends Question {
    private static final long serialVersionUID = 1L;
    private String correctAnswer;

    public ShortAnswerQuestion(int id, String questionText, int marks, String correctAnswer) {
        super(id, questionText, marks);
        this.correctAnswer = correctAnswer == null ? "" : correctAnswer.trim();
    }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String answer) { correctAnswer = answer == null ? "" : answer.trim(); }
    @Override public String getQuestionType() { return "Short Answer"; }
    @Override public void displayQuestion() { System.out.println("[Q" + getId() + "] " + getQuestionText()); }
    @Override public boolean evaluateAnswer(String answer) {
        return answer != null && normalize(answer).equals(normalize(correctAnswer));
    }
    @Override public String getCorrectAnswerDisplay() { return correctAnswer; }
    private static String normalize(String value) { return value.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT); }
}
