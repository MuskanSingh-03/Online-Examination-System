package com.exam.system.service;

import com.exam.system.model.Question;
import com.exam.system.model.Result;
import com.exam.system.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Automatic evaluation engine.
 * Employs Dynamic Polymorphism to evaluate answers across diverse Question subclasses.
 */
public class EvaluationEngine {

    /**
     * Evaluates all submitted answers against the exam questions.
     * Dynamic Polymorphism in action:
     * q.evaluateAnswer(studentAns) executes MCQQuestion or TrueFalseQuestion implementation at runtime.
     */
    public Result evaluateExam(Student student, String examTitle, List<Question> questions, Map<Integer, String> studentAnswers) {
        int totalQuestions = questions.size();
        int attemptedCount = 0;
        int correctCount = 0;
        int marksScored = 0;
        int totalPossibleMarks = 0;

        List<String> breakdown = new ArrayList<>();

        for (Question q : questions) {
            totalPossibleMarks += q.getMarks();
            String studentAns = studentAnswers.get(q.getId());

            if (studentAns != null && !studentAns.trim().isEmpty()) {
                attemptedCount++;

                // POLYMORPHIC CALL: dynamically dispatched based on concrete question type
                boolean isCorrect = q.evaluateAnswer(studentAns);

                if (isCorrect) {
                    correctCount++;
                    marksScored += q.getMarks();
                    breakdown.add(String.format("Q%d [%s]: CORRECT (+%d) | Your Answer: %s",
                            q.getId(), q.getQuestionType(), q.getMarks(), studentAns.trim()));
                } else {
                    breakdown.add(String.format("Q%d [%s]: INCORRECT (0/%d) | Your Answer: %s | Correct: %s",
                            q.getId(), q.getQuestionType(), q.getMarks(), studentAns.trim(), q.getCorrectAnswerDisplay()));
                }
            } else {
                breakdown.add(String.format("Q%d [%s]: UNATTEMPTED (0/%d) | Correct: %s",
                        q.getId(), q.getQuestionType(), q.getMarks(), q.getCorrectAnswerDisplay()));
            }
        }

        return new Result(
                student.getUsername(),
                student.getFullName(),
                examTitle,
                totalQuestions,
                attemptedCount,
                correctCount,
                marksScored,
                totalPossibleMarks,
                breakdown
        );
    }
}
