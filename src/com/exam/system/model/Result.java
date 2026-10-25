package com.exam.system.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates exam evaluation results, score aggregation, and grade calculation.
 * Implements Serializable for persistence across sessions.
 */
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentUsername;
    private String studentName;
    private String examTitle;
    private String timestamp;
    private int totalQuestions;
    private int attemptedQuestions;
    private int correctCount;
    private int incorrectCount;
    private int marksScored;
    private int totalPossibleMarks;
    private double percentage;
    private String grade;
    private String status;
    private List<String> breakdown;

    public Result(String studentUsername, String studentName, String examTitle,
                  int totalQuestions, int attemptedQuestions, int correctCount,
                  int marksScored, int totalPossibleMarks, List<String> breakdown) {
        this.studentUsername = studentUsername;
        this.studentName = studentName;
        this.examTitle = examTitle;
        this.totalQuestions = totalQuestions;
        this.attemptedQuestions = attemptedQuestions;
        this.correctCount = correctCount;
        this.incorrectCount = attemptedQuestions - correctCount;
        this.marksScored = marksScored;
        this.totalPossibleMarks = totalPossibleMarks;
        this.breakdown = new ArrayList<>(breakdown);
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        calculateGradeAndStatus();
    }

    private void calculateGradeAndStatus() {
        if (totalPossibleMarks > 0) {
            this.percentage = ((double) marksScored / totalPossibleMarks) * 100.0;
        } else {
            this.percentage = 0.0;
        }

        if (percentage >= 90.0) {
            this.grade = "A+ (Outstanding)";
            this.status = "PASS";
        } else if (percentage >= 80.0) {
            this.grade = "A (Excellent)";
            this.status = "PASS";
        } else if (percentage >= 70.0) {
            this.grade = "B (Good)";
            this.status = "PASS";
        } else if (percentage >= 50.0) {
            this.grade = "C (Satisfactory)";
            this.status = "PASS";
        } else {
            this.grade = "F (Needs Improvement)";
            this.status = "FAIL";
        }
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getAttemptedQuestions() {
        return attemptedQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public int getMarksScored() {
        return marksScored;
    }

    public int getTotalPossibleMarks() {
        return totalPossibleMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getGrade() {
        return grade;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getBreakdown() {
        return breakdown;
    }

    public void displayReportCard() {
        System.out.println("\n========================================================");
        System.out.println("               OFFICIAL EXAMINATION REPORT CARD         ");
        System.out.println("========================================================");
        System.out.printf(" Exam Title:        %s\n", examTitle);
        System.out.printf(" Student Name:      %s (ID: %s)\n", studentName, studentUsername);
        System.out.printf(" Timestamp:         %s\n", timestamp);
        System.out.println("--------------------------------------------------------");
        System.out.printf(" Total Questions:   %d\n", totalQuestions);
        System.out.printf(" Attempted:         %d | Unattempted: %d\n", attemptedQuestions, (totalQuestions - attemptedQuestions));
        System.out.printf(" Correct Answers:   %d | Incorrect:   %d\n", correctCount, incorrectCount);
        System.out.println("--------------------------------------------------------");
        System.out.printf(" Total Score:       %d / %d\n", marksScored, totalPossibleMarks);
        System.out.printf(" Percentage:        %.2f%%\n", percentage);
        System.out.printf(" Grade Assigned:    %s\n", grade);
        System.out.printf(" Final Outcome:     [%s]\n", status);
        System.out.println("========================================================");
        System.out.println("Detailed Item Analysis:");
        for (String detail : breakdown) {
            System.out.println("  • " + detail);
        }
        System.out.println("========================================================\n");
    }
}
