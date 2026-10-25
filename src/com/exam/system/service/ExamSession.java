package com.exam.system.service;

import com.exam.system.exception.TimeOutException;
import com.exam.system.model.Question;
import com.exam.system.model.Result;
import com.exam.system.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages an active timed exam session.
 * Demonstrates Concurrency / Multithreading:
 * Runs a background daemon thread to monitor remaining time and trigger auto-submission
 * when the duration expires.
 */
public class ExamSession {
    private Student student;
    private String examTitle;
    private List<Question> questions;
    private int durationSeconds;
    private EvaluationEngine evaluationEngine;

    private AtomicBoolean isTimeExpired = new AtomicBoolean(false);
    private AtomicBoolean sessionCompleted = new AtomicBoolean(false);
    private Map<Integer, String> studentAnswers = new HashMap<>();

    public ExamSession(Student student, String examTitle, List<Question> questions,
                       int durationSeconds, EvaluationEngine evaluationEngine) {
        this.student = student;
        this.examTitle = examTitle;
        this.questions = questions;
        this.durationSeconds = durationSeconds;
        this.evaluationEngine = evaluationEngine;
    }

    /**
     * Starts the timed exam session with a dedicated background timer thread.
     */
    public Result startSession(Scanner scanner) {
        System.out.println("\n========================================================");
        System.out.println("           EXAMINATION COMMENCING: " + examTitle);
        System.out.println("========================================================");
        System.out.println(" Candidate:  " + student.getFullName() + " (" + student.getRollNumber() + ")");
        System.out.println(" Questions:  " + questions.size());
        System.out.println(" Time Limit: " + durationSeconds + " seconds (Timer active in background)");
        System.out.println(" Instructions: Enter your answer number or (S) to submit early.");
        System.out.println("========================================================\n");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);

        // Multithreaded background timer daemon
        Thread timerThread = new Thread(() -> {
            boolean warned30 = false;
            boolean warned10 = false;

            while (!sessionCompleted.get() && System.currentTimeMillis() < endTime) {
                long remainingSec = (endTime - System.currentTimeMillis()) / 1000L;

                if (remainingSec <= 30 && remainingSec > 10 && !warned30) {
                    System.out.printf("\n>>> [TIMER ALERT] %d seconds remaining! <<<\n", remainingSec);
                    warned30 = true;
                }
                if (remainingSec <= 10 && remainingSec > 0 && !warned10) {
                    System.out.printf("\n>>> [FINAL WARNING] Only %d seconds left! <<<\n", remainingSec);
                    warned10 = true;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (!sessionCompleted.get() && System.currentTimeMillis() >= endTime) {
                isTimeExpired.set(true);
                System.out.println("\n\n********************************************************");
                System.out.println("  [TIME'S UP!] Exam timer has expired!");
                System.out.println("  Auto-submitting current responses to Evaluation Engine...");
                System.out.println("  Press [ENTER] to view your final scorecard.");
                System.out.println("********************************************************");
            }
        });

        timerThread.setDaemon(true);
        timerThread.start();

        try {
            for (int i = 0; i < questions.size(); i++) {
                checkTimeExpiry();

                Question q = questions.get(i);
                System.out.println("\n[Progress: Question " + (i + 1) + " of " + questions.size() + "]");
                q.displayQuestion();

                long secondsLeft = Math.max(0, (endTime - System.currentTimeMillis()) / 1000L);
                System.out.print("Your Answer (Time Left: ~" + secondsLeft + "s | 'S' to submit): ");

                String input = scanner.nextLine().trim();

                checkTimeExpiry();

                if (input.equalsIgnoreCase("S")) {
                    System.out.println("\n[EARLY SUBMISSION] Student confirmed early submission.");
                    break;
                }

                studentAnswers.put(q.getId(), input);
            }

        } catch (TimeOutException e) {
            System.out.println("\n[SYSTEM NOTICE] " + e.getMessage());
        } finally {
            // Signal timer thread to stop
            sessionCompleted.set(true);
            timerThread.interrupt();
        }

        // Automatic evaluation of submitted answers
        System.out.println("\n[EVALUATION ENGINE] Computing results and generating scorecard...");
        Result result = evaluationEngine.evaluateExam(student, examTitle, questions, studentAnswers);
        return result;
    }

    private void checkTimeExpiry() throws TimeOutException {
        if (isTimeExpired.get()) {
            throw new TimeOutException("Allocated exam session duration expired. Auto-submitting answers.");
        }
    }
}
