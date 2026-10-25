package com.exam.system;

import com.exam.system.model.Question;
import com.exam.system.model.Result;
import com.exam.system.model.Examination;
import com.exam.system.repository.FileHandler;
import com.exam.system.service.AuthService;
import com.exam.system.service.EvaluationEngine;
import com.exam.system.ui.SwingApp;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.List;

/** Offline native desktop launcher for EduExam Pro. */
public class Main {
    private static final FileHandler fileHandler = new FileHandler();
    private static final AuthService authService = new AuthService(fileHandler);
    private static final EvaluationEngine evaluationEngine = new EvaluationEngine();
    private static final List<Question> questionBank = fileHandler.loadQuestions();
    private static final List<Result> resultHistory = fileHandler.loadResults();
    private static final List<Examination> examinations = fileHandler.loadExaminations();

    public static void main(String[] args) {
        System.out.println("EduExam Pro - Offline Desktop Examination System");
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A graphical desktop is required to run EduExam Pro.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            SwingApp app = new SwingApp(fileHandler, authService, evaluationEngine, questionBank, resultHistory, examinations);
            app.setVisible(true);
        });
    }
}
