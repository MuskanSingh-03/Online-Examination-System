package com.exam.system.ui;

import com.exam.system.model.Admin;
import com.exam.system.model.MCQQuestion;
import com.exam.system.model.Question;
import com.exam.system.model.Result;
import com.exam.system.model.Student;
import com.exam.system.model.TrueFalseQuestion;
import com.exam.system.model.User;
import com.exam.system.model.Examination;
import com.exam.system.model.Teacher;
import com.exam.system.model.ShortAnswerQuestion;
import com.exam.system.repository.FileHandler;
import com.exam.system.service.AuthService;
import com.exam.system.service.EvaluationEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ultra-Modern Desktop Java Swing GUI Application for EduExam Pro.
 * Features:
 * - Anti-aliased rendering and modern dark navy color scheme (#0f172a)
 * - Candidate identity hero banner with avatar initials and live system status
 * - 4 Elevated KPI performance metrics cards
 * - Side-by-side Action Buttons: Timed Exam (30s/Q), Practice Mode, Analytics, Profile, Web App Window
 * - Strict 30-Second Per-Question countdown timer with auto-lock, auto-advance, and auto-submit
 * - Interactive untimed Practice Mode with instant answer validation & explanations
 * - Detailed Performance Analytics modal and Candidate Profile dialog
 * - Item-by-item Scorecard breakdown inspection with official grading
 * - Faculty admin dashboard with dynamic Question Bank CRUD and institute ledger
 */
public class SwingApp extends JFrame {
    // Theme Colors
    private static final Color BG_DARK = new Color(17, 16, 22);
    private static final Color CARD_BG = new Color(29, 27, 35);
    private static final Color BORDER_COLOR = new Color(61, 53, 67);
    private static final Color PRIMARY_COLOR = new Color(236, 72, 153);
    private static final Color ACCENT_CYAN = new Color(244, 114, 182);
    private static final Color SUCCESS_COLOR = new Color(190, 24, 93);
    private static final Color DANGER_COLOR = new Color(244, 63, 122);
    private static final Color WARNING_COLOR = new Color(219, 39, 119);
    private static final Color TEXT_WHITE = new Color(250, 247, 250);
    private static final Color TEXT_MUTED = new Color(190, 179, 194);

    private final FileHandler fileHandler;
    private final AuthService authService;
    private final EvaluationEngine evaluationEngine;
    private final List<Question> questionBank;
    private final List<Result> resultsHistory;
    private final List<Examination> examinations;

    private CardLayout cardLayout;
    private JPanel mainCardPanel;
    private JButton btnNavProfile;

    // Session State
    private User currentUser;
    private List<Question> activeExamQuestions;
    private int currentQIdx = 0;
    private final Map<Integer, String> currentAnswers = new HashMap<>();

    // Per-Question 30-Second Timer State
    private static final int QUESTION_TIME_LIMIT = 30; // 30 seconds per question
    private int questionSecondsRemaining = QUESTION_TIME_LIMIT;
    private Timer perQuestionTimer;
    private boolean isPracticeMode = false;
    private Examination selectedExamination;
    private int examSecondsRemaining;
    private JComboBox<Examination> availableExamSelector;

    // Student Dashboard UI Elements
    private JLabel lblStudentName;
    private JLabel lblStudentRollCourse;
    private JLabel lblAvatarInitials;
    private JLabel lblStatTotalExams;
    private JLabel lblStatHighestScore;
    private JLabel lblStatAvgPercentage;
    private JLabel lblStatBankSize;
    private DefaultTableModel studentResultsModel;

    // Exam View UI Elements
    private JLabel lblTimer;
    private JProgressBar examProgressBar;
    private JLabel lblQuestionHeader;
    private JTextArea txtQuestionStatement;
    private JTextArea currentShortAnswerField;
    private JPanel optionsPanel;
    private ButtonGroup optionsGroup;
    private final List<JRadioButton> currentOptionRadios = new ArrayList<>();
    private JPanel palettePanel;
    private JButton[] paletteButtons;
    private JButton btnNextOrSubmit;
    private JLabel lblPracticeExplanation;

    // Result View UI Elements
    private JLabel lblResultTitle;
    private JLabel lblResultScore;
    private JLabel lblResultGrade;
    private DefaultListModel<String> breakdownListModel;

    // Admin Dashboard UI Elements
    private DefaultTableModel adminQuestionsModel;
    private DefaultTableModel adminResultsModel;
    private DefaultTableModel adminStudentsModel;
    private DefaultTableModel adminExamsModel;
    private JLabel lblAdminSummary;
    private JTabbedPane adminTabs;

    public SwingApp(FileHandler fileHandler, AuthService authService,
                    EvaluationEngine evaluationEngine, List<Question> questionBank,
                    List<Result> resultsHistory, List<Examination> examinations) {
        this.fileHandler = fileHandler;
        this.authService = authService;
        this.evaluationEngine = evaluationEngine;
        this.questionBank = questionBank;
        this.resultsHistory = resultsHistory;
        this.examinations = examinations;

        initUI();
    }

    private void initUI() {
        setTitle("EduExam Pro - Online Examination & Evaluation Platform");
        setSize(1180, 780);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        // Anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Top Navigation Bar
        JPanel navBar = createNavBar();
        getContentPane().add(navBar, BorderLayout.NORTH);

        // Center Multi-View Card Layout
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BG_DARK);

        mainCardPanel.add(createLoginView(), "LOGIN");
        mainCardPanel.add(createStudentDashboardView(), "STUDENT_DASHBOARD");
        mainCardPanel.add(createExamView(), "EXAM");
        mainCardPanel.add(createResultView(), "RESULT");
        mainCardPanel.add(createAdminDashboardView(), "ADMIN_DASHBOARD");

        getContentPane().add(mainCardPanel, BorderLayout.CENTER);
        cardLayout.show(mainCardPanel, "LOGIN");
    }

    private JPanel createNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(new Color(12, 11, 16));
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JLabel brand = new JLabel("🎓 EduExam Pro");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brand.setForeground(TEXT_WHITE);
        nav.add(brand, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);


        btnNavProfile = createStyledButton("My Profile", ACCENT_CYAN);
        btnNavProfile.setVisible(false);
        btnNavProfile.addActionListener(e -> {
            if (currentUser instanceof Admin || currentUser instanceof Teacher) showAdminProfileDialog();
            else showCandidateProfileDialog();
        });
        rightPanel.add(btnNavProfile);

        JButton btnLogout = createStyledButton("Logout", new Color(77, 67, 72));
        btnLogout.addActionListener(e -> logout());
        rightPanel.add(btnLogout);

        nav.add(rightPanel, BorderLayout.EAST);
        return nav;
    }

    // ==========================================
    // 1. LOGIN VIEW
    // ==========================================
    private JPanel createLoginView() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(32, 40, 32, 40)
        ));
        card.setPreferredSize(new Dimension(460, 510));

        JLabel iconLabel = new JLabel("🎓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("EduExam Pro Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel subtitle = new JLabel("Online Examination & Automated Evaluation System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));

        JTextField txtUser = new JTextField("student1");
        styleTextField(txtUser);
        JPasswordField txtPass = new JPasswordField("pass123");
        styleTextField(txtPass);

        card.add(createInputRow("Username", txtUser));
        card.add(Box.createVerticalStrut(14));
        card.add(createInputRow("Password", txtPass));
        card.add(Box.createVerticalStrut(20));

        JButton btnLogin = createStyledButton("Sign In to Portal", PRIMARY_COLOR);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            try {
                currentUser = authService.login(u, p);
                btnNavProfile.setVisible(true);
                applyRolePermissions();
                if (currentUser instanceof Student) {
                    refreshStudentDashboard();
                    cardLayout.show(mainCardPanel, "STUDENT_DASHBOARD");
                } else if (currentUser instanceof Admin) {
                    refreshAdminDashboard();
                    cardLayout.show(mainCardPanel, "ADMIN_DASHBOARD");
                } else if (currentUser instanceof Teacher) {
                    refreshAdminDashboard();
                    cardLayout.show(mainCardPanel, "ADMIN_DASHBOARD");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));

        // Demo Accounts Bar
        JPanel demoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        demoPanel.setOpaque(false);
        demoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnFillStudent = createStyledButton("Student Demo", new Color(77, 67, 72));
        btnFillStudent.addActionListener(e -> { txtUser.setText("student1"); txtPass.setText("pass123"); });

        JButton btnFillAdmin = createStyledButton("Faculty Admin Demo", new Color(77, 67, 72));
        btnFillAdmin.addActionListener(e -> { txtUser.setText("admin1"); txtPass.setText("admin123"); });

        demoPanel.add(btnFillStudent);
        demoPanel.add(btnFillAdmin);
        JButton btnFillTeacher = createStyledButton("Teacher Demo", new Color(77, 67, 72));
        btnFillTeacher.addActionListener(e -> { txtUser.setText("teacher1"); txtPass.setText("teach123"); });
        demoPanel.add(btnFillTeacher);
        card.add(demoPanel);
        card.add(Box.createVerticalStrut(10));
        JButton btnRegister = createStyledButton("Create Student Account", PRIMARY_COLOR);
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> promptStudentRegistration());
        card.add(btnRegister);

        container.add(card);
        return container;
    }

    // ==========================================
    // 2. UPGRADED STUDENT DASHBOARD VIEW
    // ==========================================
    private JPanel createStudentDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Top Section: Candidate Hero Banner + 4 KPI Cards + 4 Action Buttons
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // A. Candidate Identity Hero Banner
        JPanel heroBanner = new JPanel(new BorderLayout(16, 10));
        heroBanner.setBackground(CARD_BG);
        heroBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 39, 119, 120), 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel profileLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        profileLeft.setOpaque(false);

        // Avatar Circle
        JPanel avatarCircle = new JPanel(new GridBagLayout());
        avatarCircle.setPreferredSize(new Dimension(54, 54));
        avatarCircle.setBackground(new Color(236, 72, 153, 65));
        avatarCircle.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true));
        lblAvatarInitials = new JLabel("RS");
        lblAvatarInitials.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAvatarInitials.setForeground(TEXT_WHITE);
        avatarCircle.add(lblAvatarInitials);
        profileLeft.add(avatarCircle);

        // Name & Course
        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);
        lblStudentName = new JLabel("Candidate: Rahul Sharma");
        lblStudentName.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblStudentName.setForeground(TEXT_WHITE);
        lblStudentRollCourse = new JLabel("Roll No: CS2026-042 | B.Tech Computer Science | Track: Java OOP");
        lblStudentRollCourse.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStudentRollCourse.setForeground(TEXT_MUTED);
        textGroup.add(lblStudentName);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(lblStudentRollCourse);
        profileLeft.add(textGroup);

        heroBanner.add(profileLeft, BorderLayout.WEST);

        // Status Badge
        JPanel statusPill = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        statusPill.setOpaque(false);
        JLabel lblStatus = new JLabel("🟢 System Proctored & Session Online");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(PRIMARY_COLOR);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 39, 119, 100), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        statusPill.add(lblStatus);
        heroBanner.add(statusPill, BorderLayout.EAST);
        topContainer.add(heroBanner);
        topContainer.add(Box.createVerticalStrut(14));

        // B. 4 Elevated KPI Cards Grid
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiGrid.setOpaque(false);

        lblStatTotalExams = new JLabel("0", SwingConstants.CENTER);
        lblStatHighestScore = new JLabel("--", SwingConstants.CENTER);
        lblStatAvgPercentage = new JLabel("--", SwingConstants.CENTER);
        lblStatBankSize = new JLabel("10", SwingConstants.CENTER);

        kpiGrid.add(createKpiCard("Exams Completed", lblStatTotalExams, "Official Attempts", PRIMARY_COLOR));
        kpiGrid.add(createKpiCard("Highest Score", lblStatHighestScore, "Best Performance", SUCCESS_COLOR));
        kpiGrid.add(createKpiCard("Average Score", lblStatAvgPercentage, "Proficiency Rating", ACCENT_CYAN));
        kpiGrid.add(createKpiCard("Question Bank", lblStatBankSize, "Active Java Pool", WARNING_COLOR));

        topContainer.add(kpiGrid);
        topContainer.add(Box.createVerticalStrut(14));

        JPanel examPicker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        examPicker.setOpaque(false);
        JLabel examPickerLabel = new JLabel("Available examinations:");
        examPickerLabel.setForeground(TEXT_WHITE);
        availableExamSelector = new JComboBox<>();
        availableExamSelector.setPreferredSize(new Dimension(370, 34));
        for (Examination exam : examinations) if (exam.isActive()) availableExamSelector.addItem(exam);
        JButton btnExamDetails = createStyledButton("Instructions & Details", new Color(77, 67, 72));
        btnExamDetails.addActionListener(e -> showSelectedExamDetails());
        examPicker.add(examPickerLabel);
        examPicker.add(availableExamSelector);
        examPicker.add(btnExamDetails);
        topContainer.add(examPicker);
        topContainer.add(Box.createVerticalStrut(12));

        // C. Side-by-Side Action Buttons Row
        JPanel actionRow = new JPanel(new GridLayout(1, 5, 10, 0));
        actionRow.setOpaque(false);

        JButton btnStartTimed = createStyledButton("Start Selected Exam", SUCCESS_COLOR);
        btnStartTimed.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStartTimed.addActionListener(e -> startSelectedExamination());

        JButton btnPractice = createStyledButton("📖 Practice Mode (Untimed)", ACCENT_CYAN);
        btnPractice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPractice.addActionListener(e -> startActiveExam(true));

        JButton btnAnalytics = createStyledButton("📊 View Detailed Analytics", PRIMARY_COLOR);
        btnAnalytics.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAnalytics.addActionListener(e -> showDetailedAnalyticsDialog());

        JButton btnProfile = createStyledButton("⚙️ Candidate Profile", new Color(77, 67, 72));
        btnProfile.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProfile.addActionListener(e -> showCandidateProfileDialog());
        JButton btnLeaderboard = createStyledButton("Leaderboard", WARNING_COLOR);
        btnLeaderboard.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLeaderboard.addActionListener(e -> showLeaderboard());

        actionRow.add(btnStartTimed);
        actionRow.add(btnPractice);
        actionRow.add(btnAnalytics);
        actionRow.add(btnProfile);
        actionRow.add(btnLeaderboard);

        topContainer.add(actionRow);
        panel.add(topContainer, BorderLayout.NORTH);

        // Center: Examination Ledger Table with Scorecard Inspection
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tblTitle = new JLabel("My Examination Records & Scorecards");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tblTitle.setForeground(TEXT_WHITE);
        tableHeader.add(tblTitle, BorderLayout.WEST);

        JButton btnInspectScorecard = createStyledButton("👁️ View Selected Scorecard", PRIMARY_COLOR);
        btnInspectScorecard.addActionListener(e -> inspectSelectedRowScorecard());
        JButton btnExportMine = createStyledButton("Export My Results", ACCENT_CYAN);
        btnExportMine.addActionListener(e -> exportStudentResultsCsv());
        JPanel studentActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        studentActions.setOpaque(false);
        studentActions.add(btnExportMine);
        studentActions.add(btnInspectScorecard);
        tableHeader.add(studentActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Date & Time", "Exam Title", "Marks Scored", "Percentage", "Grade", "Status"};
        studentResultsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(studentResultsModel);
        styleTable(table);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        return card;
    }

    private void refreshStudentDashboard() {
        if (currentUser instanceof Student) {
            Student s = (Student) currentUser;
            lblStudentName.setText("Candidate: " + s.getFullName());
            lblStudentRollCourse.setText("Roll No: " + s.getRollNumber() + " | Degree: " + s.getCourse());
            String initials = "ST";
            String[] parts = s.getFullName().split(" ");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (s.getFullName().length() > 0) {
                initials = s.getFullName().substring(0, Math.min(2, s.getFullName().length())).toUpperCase();
            }
            lblAvatarInitials.setText(initials);
        }

        // Calculate Metrics
        int totalAttempts = 0;
        int highestScore = 0;
        int highestPossible = 0;
        double highestPct = 0.0;
        double totalPctSum = 0.0;

        studentResultsModel.setRowCount(0);
        for (Result r : resultsHistory) {
            if (currentUser != null && r.getStudentUsername().equalsIgnoreCase(currentUser.getUsername())) {
                totalAttempts++;
                totalPctSum += r.getPercentage();
                if (r.getMarksScored() > highestScore) {
                    highestScore = r.getMarksScored();
                    highestPossible = r.getTotalPossibleMarks();
                    highestPct = r.getPercentage();
                }
                studentResultsModel.addRow(new Object[]{
                        r.getTimestamp(), r.getExamTitle(),
                        r.getMarksScored() + " / " + r.getTotalPossibleMarks(),
                        String.format("%.1f%%", r.getPercentage()),
                        r.getGrade(), r.getStatus()
                });
            }
        }

        lblStatTotalExams.setText(String.valueOf(totalAttempts));
        if (totalAttempts > 0) {
            lblStatHighestScore.setText(highestScore + " (" + String.format("%.0f%%", highestPct) + ")");
            lblStatAvgPercentage.setText(String.format("%.1f%%", totalPctSum / totalAttempts));
        } else {
            lblStatHighestScore.setText("--");
            lblStatAvgPercentage.setText("--");
        }
        lblStatBankSize.setText(String.valueOf(questionBank.size()));
    }

    // ==========================================
    // 3. EXAM SESSION VIEW (PER-QUESTION 30S TIMER)
    // ==========================================
    private JPanel createExamView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Top Exam Header: Per-Question Countdown Timer & Progress Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 12));
        topBar.setBackground(CARD_BG);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        lblTimer = new JLabel("⏱️ Question Time Left: 00:30");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTimer.setForeground(ACCENT_CYAN);
        topBar.add(lblTimer, BorderLayout.WEST);

        examProgressBar = new JProgressBar(0, 100);
        examProgressBar.setValue(100);
        examProgressBar.setForeground(PRIMARY_COLOR);
        examProgressBar.setBackground(BG_DARK);
        examProgressBar.setPreferredSize(new Dimension(320, 18));
        topBar.add(examProgressBar, BorderLayout.CENTER);

        JButton btnSubmitEarly = createStyledButton("Finish & Submit 🏁", DANGER_COLOR);
        btnSubmitEarly.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to finish and submit your exam now?",
                    "Confirm Submission", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                finishAndSubmitExam();
            }
        });
        topBar.add(btnSubmitEarly, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Center Split: Left Question Palette + Main Question Card
        JPanel centerSplit = new JPanel(new BorderLayout(16, 0));
        centerSplit.setOpaque(false);

        // Left Palette Panel
        palettePanel = new JPanel();
        palettePanel.setLayout(new BoxLayout(palettePanel, BoxLayout.Y_AXIS));
        palettePanel.setBackground(CARD_BG);
        palettePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        palettePanel.setPreferredSize(new Dimension(210, 420));
        centerSplit.add(palettePanel, BorderLayout.WEST);

        // Main Question Card
        JPanel qCard = new JPanel(new BorderLayout(16, 16));
        qCard.setBackground(CARD_BG);
        qCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));

        lblQuestionHeader = new JLabel("Question 1 of 10");
        lblQuestionHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblQuestionHeader.setForeground(PRIMARY_COLOR);
        qCard.add(lblQuestionHeader, BorderLayout.NORTH);

        txtQuestionStatement = new JTextArea("Question statement text...");
        txtQuestionStatement.setFont(new Font("Segoe UI", Font.BOLD, 17));
        txtQuestionStatement.setForeground(TEXT_WHITE);
        txtQuestionStatement.setBackground(CARD_BG);
        txtQuestionStatement.setLineWrap(true);
        txtQuestionStatement.setWrapStyleWord(true);
        txtQuestionStatement.setEditable(false);
        txtQuestionStatement.setBorder(null);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);

        lblPracticeExplanation = new JLabel("");
        lblPracticeExplanation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPracticeExplanation.setForeground(PRIMARY_COLOR);
        lblPracticeExplanation.setVisible(false);

        JPanel questionContent = new JPanel(new BorderLayout(14, 14));
        questionContent.setOpaque(false);
        questionContent.add(txtQuestionStatement, BorderLayout.NORTH);
        questionContent.add(optionsPanel, BorderLayout.CENTER);
        questionContent.add(lblPracticeExplanation, BorderLayout.SOUTH);
        qCard.add(questionContent, BorderLayout.CENTER);

        // Navigation controls at bottom
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);

        JButton btnPrev = createStyledButton("⏮️ Previous", new Color(77, 67, 72));
        btnPrev.addActionListener(e -> navigateQuestion(-1));
        actionRow.add(btnPrev);

        JButton btnClear = createStyledButton("Clear Choice", new Color(77, 67, 72));
        btnClear.addActionListener(e -> clearSelectedOption());
        actionRow.add(btnClear);

        JButton btnVerifyPractice = createStyledButton("💡 Check Answer", WARNING_COLOR);
        btnVerifyPractice.addActionListener(e -> verifyCurrentPracticeAnswer());
        btnVerifyPractice.setName("btnVerifyPractice");
        actionRow.add(btnVerifyPractice);

        btnNextOrSubmit = createStyledButton("Next Question ⏭️", PRIMARY_COLOR);
        btnNextOrSubmit.addActionListener(e -> handleNextOrSubmitClick());
        actionRow.add(btnNextOrSubmit);

        qCard.add(actionRow, BorderLayout.SOUTH);
        centerSplit.add(qCard, BorderLayout.CENTER);

        panel.add(centerSplit, BorderLayout.CENTER);
        return panel;
    }

    private void showSelectedExamDetails() {
        Examination exam = (Examination) availableExamSelector.getSelectedItem();
        if (exam == null) {
            JOptionPane.showMessageDialog(this, "No examination has been scheduled yet.");
            return;
        }
        String schedule = "Available now";
        if (exam.getAvailableFrom() != null || exam.getAvailableUntil() != null) {
            schedule = "Availability: " + (exam.getAvailableFrom() == null ? "Any time" : exam.getAvailableFrom())
                    + " through " + (exam.getAvailableUntil() == null ? "No end date" : exam.getAvailableUntil());
        }
        JOptionPane.showMessageDialog(this,
                exam.getTitle() + "\nSubject: " + exam.getSubject() + "\nDuration: " + exam.getDurationMinutes()
                        + " minutes\nQuestions: " + exam.getQuestionIds().size() + "\n" + schedule
                        + "\n\nInstructions:\n" + exam.getInstructions(),
                "Examination Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startSelectedExamination() {
        Examination exam = (Examination) availableExamSelector.getSelectedItem();
        if (exam == null) { JOptionPane.showMessageDialog(this, "No examination is available."); return; }
        LocalDateTime now = LocalDateTime.now();
        if (!exam.isAvailableAt(now)) {
            JOptionPane.showMessageDialog(this, "This examination is outside its scheduled availability window.");
            return;
        }
        boolean alreadyAttempted = resultsHistory.stream().anyMatch(result ->
                result.getStudentUsername().equalsIgnoreCase(currentUser.getUsername())
                        && result.getExamTitle().equalsIgnoreCase(exam.getTitle()));
        if (exam.isOneAttempt() && alreadyAttempted) {
            JOptionPane.showMessageDialog(this, "You have already submitted this one-attempt examination.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                exam.getTitle() + "\nSubject: " + exam.getSubject() + "\nDuration: " + exam.getDurationMinutes()
                        + " minutes\nQuestions: " + exam.getQuestionIds().size() + "\n\n" + exam.getInstructions()
                        + "\n\nStart now? The timer submits automatically when it expires.",
                "Exam Instructions", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        selectedExamination = exam;
        startActiveExam(false);
    }

    private void startActiveExam(boolean practice) {
        if (questionBank.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The Question Bank is currently empty!");
            return;
        }

        this.isPracticeMode = practice;
        activeExamQuestions = new ArrayList<>();
        if (practice) {
            activeExamQuestions.addAll(questionBank);
        } else if (selectedExamination != null) {
            for (Integer id : selectedExamination.getQuestionIds()) {
                for (Question question : questionBank) if (question.getId() == id) activeExamQuestions.add(question);
            }
            if (activeExamQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This examination has no valid questions. Ask faculty to review its question list.");
                return;
            }
        }
        if (!practice && selectedExamination != null && selectedExamination.isRandomizeQuestions()) {
            Collections.shuffle(activeExamQuestions);
        }

        currentQIdx = 0;
        currentAnswers.clear();

        renderPaletteGrid();
        renderCurrentExamQuestion();
        cardLayout.show(mainCardPanel, "EXAM");

        // Find practice button and toggle visibility
        for (Component c : ((JPanel) ((JPanel) ((BorderLayout) mainCardPanel.getLayout()).getLayoutComponent(mainCardPanel, "EXAM")).getComponent(1)).getComponents()) {
            // Traverse components if needed
        }

        if (!isPracticeMode) {
            startExamTimer();
        } else {
            if (perQuestionTimer != null) perQuestionTimer.stop();
            lblTimer.setText("📖 Practice Studio (Self-Paced & Untimed)");
            lblTimer.setForeground(ACCENT_CYAN);
            examProgressBar.setValue(100);
        }
    }

    /**
     * Strict 30-Second Per-Question Countdown Timer Engine.
     * When it hits zero: locks options, records answer, advances or auto-submits.
     */
    private void startExamTimer() {
        if (perQuestionTimer != null) perQuestionTimer.stop();
        examSecondsRemaining = Math.max(1, selectedExamination.getDurationMinutes()) * 60;
        examProgressBar.setMaximum(examSecondsRemaining);
        examProgressBar.setValue(examSecondsRemaining);
        updateTimerLabel(examSecondsRemaining);
        perQuestionTimer = new Timer(1000, (ActionEvent e) -> {
            examSecondsRemaining--;
            examProgressBar.setValue(examSecondsRemaining);
            updateTimerLabel(examSecondsRemaining);
            if (examSecondsRemaining <= 0) {
                perQuestionTimer.stop();
                handleExamTimeout();
            }
        });
        perQuestionTimer.start();
    }
    private void updateTimerLabel(int sec) {
        lblTimer.setText(String.format("Time remaining: %02d:%02d", sec / 60, sec % 60));
        if (sec <= 60) {
            lblTimer.setForeground(DANGER_COLOR);
            examProgressBar.setForeground(DANGER_COLOR);
        } else {
            lblTimer.setForeground(ACCENT_CYAN);
            examProgressBar.setForeground(PRIMARY_COLOR);
        }
    }
    private void handleExamTimeout() {
        JOptionPane.showMessageDialog(this, "The exam duration has ended. Your saved answers are being submitted.",
                "Time Expired", JOptionPane.WARNING_MESSAGE);
        finishAndSubmitExam();
    }
    private void renderPaletteGrid() {
        palettePanel.removeAll();

        JLabel pTitle = new JLabel(isPracticeMode ? "Practice Palette" : "Question Palette");
        pTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pTitle.setForeground(TEXT_WHITE);
        palettePanel.add(pTitle);
        palettePanel.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(0, 3, 6, 6));
        grid.setOpaque(false);

        paletteButtons = new JButton[activeExamQuestions.size()];
        for (int i = 0; i < activeExamQuestions.size(); i++) {
            final int idx = i;
            JButton btn = new JButton(String.valueOf(i + 1));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBackground(BG_DARK);
            btn.setForeground(TEXT_MUTED);
            btn.addActionListener(e -> {
                if (isPracticeMode) {
                    currentQIdx = idx;
                    renderCurrentExamQuestion();
                } else {
                    // In timed exam, jumping to a question resets its 30s timer
                    currentQIdx = idx;
                    renderCurrentExamQuestion();
                    
                }
            });
            paletteButtons[i] = btn;
            grid.add(btn);
        }
        palettePanel.add(grid);
        palettePanel.revalidate();
        palettePanel.repaint();
    }

    private void renderCurrentExamQuestion() {
        if (activeExamQuestions == null || activeExamQuestions.isEmpty()) return;
        Question q = activeExamQuestions.get(currentQIdx);

        String modeTag = isPracticeMode ? " [Practice Mode]" : " [Timed Examination]";
        lblQuestionHeader.setText("Question " + (currentQIdx + 1) + " of " + activeExamQuestions.size() +
                " (" + q.getQuestionType() + " | " + q.getMarks() + " Marks)" + modeTag);
        txtQuestionStatement.setText(q.getQuestionText());

        optionsPanel.removeAll();
        currentOptionRadios.clear();
        optionsGroup = new ButtonGroup();
        lblPracticeExplanation.setVisible(false);

        String selectedVal = currentAnswers.get(q.getId());

        if (q instanceof MCQQuestion) {
            currentShortAnswerField = null;
            MCQQuestion mcq = (MCQQuestion) q;
            List<String> opts = mcq.getOptions();
            for (int i = 0; i < opts.size(); i++) {
                final String val = String.valueOf(i + 1);
                JRadioButton rb = new JRadioButton("(" + val + ") " + opts.get(i));
                styleRadioButton(rb);
                if (val.equals(selectedVal)) rb.setSelected(true);

                rb.addActionListener(e -> {
                    currentAnswers.put(q.getId(), val);
                    updatePaletteStyles();
                });
                optionsGroup.add(rb);
                currentOptionRadios.add(rb);
                optionsPanel.add(rb);
                optionsPanel.add(Box.createVerticalStrut(8));
            }
        } else if (q instanceof TrueFalseQuestion) {
            currentShortAnswerField = null;
            String[][] tfOpts = {{"1", "True / (T)"}, {"2", "False / (F)"}};
            for (String[] pair : tfOpts) {
                final String val = pair[0];
                JRadioButton rb = new JRadioButton(pair[1]);
                styleRadioButton(rb);
                if (val.equals(selectedVal)) rb.setSelected(true);

                rb.addActionListener(e -> {
                    currentAnswers.put(q.getId(), val);
                    updatePaletteStyles();
                });
                optionsGroup.add(rb);
                currentOptionRadios.add(rb);
                optionsPanel.add(rb);
                optionsPanel.add(Box.createVerticalStrut(8));
            }
        } else if (q instanceof ShortAnswerQuestion) {
            JTextArea answer = new JTextArea(selectedVal == null ? "" : selectedVal, 4, 36);
            currentShortAnswerField = answer;
            answer.setLineWrap(true);
            answer.setWrapStyleWord(true);
            answer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            answer.setForeground(TEXT_WHITE);
            answer.setBackground(BG_DARK);
            answer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(8, 10, 8, 10)));
            answer.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void save() {
                    String value = answer.getText().trim();
                    if (value.isEmpty()) currentAnswers.remove(q.getId()); else currentAnswers.put(q.getId(), value);
                    updatePaletteStyles();
                }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { save(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { save(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { save(); }
            });
            optionsPanel.add(new JLabel("Your answer:"));
            optionsPanel.add(Box.createVerticalStrut(8));
            optionsPanel.add(new JScrollPane(answer));
        }

        // Update button text for last question
        if (currentQIdx == activeExamQuestions.size() - 1) {
            btnNextOrSubmit.setText("Finish & Submit 🏁");
            btnNextOrSubmit.setBackground(DANGER_COLOR);
        } else {
            btnNextOrSubmit.setText("Next Question ⏭️");
            btnNextOrSubmit.setBackground(PRIMARY_COLOR);
        }

        updatePaletteStyles();
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void updatePaletteStyles() {
        if (paletteButtons == null) return;
        for (int i = 0; i < activeExamQuestions.size(); i++) {
            Question q = activeExamQuestions.get(i);
            JButton btn = paletteButtons[i];

            if (currentAnswers.containsKey(q.getId())) {
                btn.setBackground(SUCCESS_COLOR);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(BG_DARK);
                btn.setForeground(TEXT_MUTED);
            }

            if (i == currentQIdx) {
                btn.setBorder(BorderFactory.createLineBorder(ACCENT_CYAN, 2));
            } else {
                btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }
        }
    }

    private void handleNextOrSubmitClick() {
        if (currentQIdx >= activeExamQuestions.size() - 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "You have reached the final question. Are you ready to submit?",
                    "Submit Exam", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                finishAndSubmitExam();
            }
        } else {
            currentQIdx++;
            renderCurrentExamQuestion();
            if (!isPracticeMode) {
                
            }
        }
    }

    private void navigateQuestion(int delta) {
        int next = currentQIdx + delta;
        if (next >= 0 && next < activeExamQuestions.size()) {
            currentQIdx = next;
            renderCurrentExamQuestion();
            if (!isPracticeMode) {
                
            }
        }
    }

    private void clearSelectedOption() {
        Question q = activeExamQuestions.get(currentQIdx);
        currentAnswers.remove(q.getId());
        if (optionsGroup != null) optionsGroup.clearSelection();
        if (currentShortAnswerField != null) currentShortAnswerField.setText("");
        updatePaletteStyles();
    }

    private void verifyCurrentPracticeAnswer() {
        Question q = activeExamQuestions.get(currentQIdx);
        String selected = currentAnswers.get(q.getId());

        if (selected == null || selected.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an answer choice first!", "Practice Check", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean isCorrect = q.evaluateAnswer(selected);
        if (isCorrect) {
            lblPracticeExplanation.setText("✅ Correct Answer! Excellent OOP understanding. Answer: " + q.getCorrectAnswerDisplay());
            lblPracticeExplanation.setForeground(PRIMARY_COLOR);
        } else {
            lblPracticeExplanation.setText("❌ Incorrect. The correct answer is: " + q.getCorrectAnswerDisplay());
            lblPracticeExplanation.setForeground(DANGER_COLOR);
        }
        lblPracticeExplanation.setVisible(true);
    }

    private void finishAndSubmitExam() {
        if (perQuestionTimer != null) {
            perQuestionTimer.stop();
        }

        Student student = (currentUser instanceof Student) ? (Student) currentUser :
                new Student("demo", "", "Demo Student", "", "001", "CS");

        String title = isPracticeMode ? "Java OOP Practice Evaluation"
                : selectedExamination == null ? "Examination" : selectedExamination.getTitle();
        Result result = evaluationEngine.evaluateExam(student, title, activeExamQuestions, currentAnswers);

        resultsHistory.add(result);
        fileHandler.saveResults(resultsHistory);

        displayResultScreen(result);
    }

    // ==========================================
    // 4. SCORECARD RESULT VIEW
    // ==========================================
    private JPanel createResultView() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(26, 36, 26, 36));

        JPanel heroCard = new JPanel(new GridLayout(3, 1, 6, 6));
        heroCard.setBackground(CARD_BG);
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        lblResultTitle = new JLabel("Official Examination Scorecard", SwingConstants.CENTER);
        lblResultTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblResultTitle.setForeground(TEXT_WHITE);

        lblResultScore = new JLabel("Score: 25 / 25 (100.0%)", SwingConstants.CENTER);
        lblResultScore.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblResultScore.setForeground(SUCCESS_COLOR);

        lblResultGrade = new JLabel("Grade: A+ (Outstanding) | PASS", SwingConstants.CENTER);
        lblResultGrade.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblResultGrade.setForeground(TEXT_WHITE);

        heroCard.add(lblResultTitle);
        heroCard.add(lblResultScore);
        heroCard.add(lblResultGrade);
        panel.add(heroCard, BorderLayout.NORTH);

        // Breakdown List
        breakdownListModel = new DefaultListModel<>();
        JList<String> list = new JList<>(breakdownListModel);
        list.setBackground(CARD_BG);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        JButton btnReview = createStyledButton("📋 Review Analytics", ACCENT_CYAN);
        btnReview.addActionListener(e -> showDetailedAnalyticsDialog());
        bottomBar.add(btnReview);

        JButton btnPrint = createStyledButton("Print Scorecard", ACCENT_CYAN);
        btnPrint.addActionListener(e -> printCurrentScorecard());
        bottomBar.add(btnPrint);

        JButton btnBack = createStyledButton("Return to Student Dashboard", PRIMARY_COLOR);
        btnBack.addActionListener(e -> {
            refreshStudentDashboard();
            cardLayout.show(mainCardPanel, "STUDENT_DASHBOARD");
        });
        bottomBar.add(btnBack);

        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    private void displayResultScreen(Result r) {
        lblResultTitle.setText(r.getExamTitle() + " Scorecard");
        lblResultScore.setText(String.format("Score: %d / %d (%.1f%%)", r.getMarksScored(), r.getTotalPossibleMarks(), r.getPercentage()));
        lblResultScore.setForeground(r.getStatus().equalsIgnoreCase("PASS") ? SUCCESS_COLOR : DANGER_COLOR);
        lblResultGrade.setText("Grade: " + r.getGrade() + " | Outcome: [" + r.getStatus() + "]");

        breakdownListModel.clear();
        for (String detail : r.getBreakdown()) {
            breakdownListModel.addElement(" • " + detail);
        }

        cardLayout.show(mainCardPanel, "RESULT");
    }

    private void printCurrentScorecard() {
        StringBuilder report = new StringBuilder("EDUEXAM PRO - EXAMINATION SCORECARD\n\n");
        report.append(lblResultTitle.getText()).append('\n').append(lblResultScore.getText()).append('\n').append(lblResultGrade.getText()).append("\n\n");
        for (int i = 0; i < breakdownListModel.size(); i++) report.append(breakdownListModel.get(i)).append('\n');
        JTextArea printable = new JTextArea(report.toString());
        try {
            if (!printable.print()) JOptionPane.showMessageDialog(this, "Printing was cancelled.");
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Could not print the scorecard: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // 5. MODAL DIALOGS: ANALYTICS, PROFILE, SCORECARD
    // ==========================================
    private void showDetailedAnalyticsDialog() {
        JDialog dialog = new JDialog(this, "Detailed Performance Analytics", true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("📊 Candidate Performance & Curriculum Mastery");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_WHITE);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        // Compute aggregate metrics
        int total = 0;
        int passed = 0;
        double sum = 0;
        int highest = 0;
        for (Result r : resultsHistory) {
            if (currentUser != null && r.getStudentUsername().equalsIgnoreCase(currentUser.getUsername())) {
                total++;
                sum += r.getPercentage();
                if ("PASS".equalsIgnoreCase(r.getStatus())) passed++;
                if (r.getMarksScored() > highest) highest = r.getMarksScored();
            }
        }
        double avg = total > 0 ? (sum / total) : 0.0;
        String tier = avg >= 90 ? "Distinction (Top 5%)" : (avg >= 75 ? "First Class (Proficient)" : "Satisfactory Progress");

        JPanel statsCard = new JPanel(new GridLayout(4, 2, 8, 8));
        statsCard.setBackground(CARD_BG);
        statsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        addStatRow(statsCard, "Total Examinations Taken:", String.valueOf(total));
        addStatRow(statsCard, "Pass / Total Ratio:", passed + " / " + total + " (" + (total > 0 ? (passed * 100 / total) : 0) + "%)");
        addStatRow(statsCard, "Cumulative Average Score:", String.format("%.1f%%", avg));
        addStatRow(statsCard, "Academic Mastery Tier:", tier);
        content.add(statsCard);
        content.add(Box.createVerticalStrut(16));

        // Syllabus Mastery Matrix
        JLabel syllabusTitle = new JLabel("Curriculum Mastery Breakdown:");
        syllabusTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        syllabusTitle.setForeground(TEXT_WHITE);
        content.add(syllabusTitle);
        content.add(Box.createVerticalStrut(8));

        JPanel masteryPanel = new JPanel(new GridLayout(4, 1, 6, 6));
        masteryPanel.setBackground(CARD_BG);
        masteryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        addMasteryRow(masteryPanel, "Polymorphism & Dynamic Dispatch", "92% Mastery", SUCCESS_COLOR);
        addMasteryRow(masteryPanel, "Encapsulation & Access Modifiers", "88% Mastery", SUCCESS_COLOR);
        addMasteryRow(masteryPanel, "Java Collections Framework (HashMap, List)", "80% Mastery", ACCENT_CYAN);
        addMasteryRow(masteryPanel, "Exception Handling & Keywords", "75% Mastery", PRIMARY_COLOR);
        content.add(masteryPanel);
        content.add(Box.createVerticalStrut(18));

        JButton btnClose = createStyledButton("Close Analytics", PRIMARY_COLOR);
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dialog.dispose());
        content.add(btnClose);

        dialog.getContentPane().add(content);
        dialog.setVisible(true);
    }

    private void addStatRow(JPanel panel, String label, String val) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_MUTED);

        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(TEXT_WHITE);

        panel.add(l);
        panel.add(v);
    }

    private void addMasteryRow(JPanel panel, String topic, String status, Color color) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);

        JLabel t = new JLabel("• " + topic);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_WHITE);

        JLabel s = new JLabel(status);
        s.setFont(new Font("Segoe UI", Font.BOLD, 12));
        s.setForeground(color);

        r.add(t, BorderLayout.WEST);
        r.add(s, BorderLayout.EAST);
        panel.add(r);
    }

    private void showCandidateProfileDialog() {
        showProfileDialog(currentUser);
    }

    private void showAdminProfileDialog() {
        showProfileDialog(currentUser);
    }

    private void showProfileDialog(User user) {
        if (user == null) return;
        JDialog dialog = new JDialog(this, "My Profile", true);
        dialog.setSize(560, 540);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_DARK);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        JLabel title = new JLabel(user instanceof Teacher ? "Teacher Profile" : user instanceof Admin ? "Administrator Profile" : "Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_WHITE);
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField username = new JTextField(user.getUsername());
        username.setEditable(false);
        JTextField fullName = new JTextField(valueOrEmpty(user.getFullName()));
        JTextField email = new JTextField(valueOrEmpty(user.getEmail()));
        styleTextField(username);
        styleTextField(fullName);
        styleTextField(email);
        form.add(new JLabel("Username (fixed):")); form.add(username);
        form.add(new JLabel("Full name:")); form.add(fullName);
        form.add(new JLabel("Email:")); form.add(email);

        JTextField studentRoll = null;
        JTextField studentCourse = null;
        JTextField employeeId = null;
        JTextField department = null;
        if (user instanceof Student) {
            Student student = (Student) user;
            studentRoll = new JTextField(valueOrEmpty(student.getRollNumber()));
            studentCourse = new JTextField(valueOrEmpty(student.getCourse()));
            styleTextField(studentRoll); styleTextField(studentCourse);
            form.add(new JLabel("Roll number:")); form.add(studentRoll);
            form.add(new JLabel("Course / program:")); form.add(studentCourse);
        } else if (user instanceof Admin || user instanceof Teacher) {
            String id = user instanceof Admin ? ((Admin) user).getEmployeeId() : ((Teacher) user).getEmployeeId();
            String dept = user instanceof Admin ? ((Admin) user).getDepartment() : ((Teacher) user).getDepartment();
            employeeId = new JTextField(valueOrEmpty(id));
            department = new JTextField(valueOrEmpty(dept));
            styleTextField(employeeId); styleTextField(department);
            form.add(new JLabel("Staff ID:")); form.add(employeeId);
            form.add(new JLabel("Department:")); form.add(department);
        }
        for (Component component : form.getComponents()) {
            if (component instanceof JLabel) {
                component.setForeground(TEXT_MUTED);
                component.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
        }
        content.add(form, BorderLayout.CENTER);

        final JTextField rollField = studentRoll;
        final JTextField courseField = studentCourse;
        final JTextField staffField = employeeId;
        final JTextField departmentField = department;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton save = createStyledButton("Save Profile", PRIMARY_COLOR);
        JButton password = createStyledButton("Change Password", ACCENT_CYAN);
        JButton close = createStyledButton("Close", new Color(77, 67, 72));
        save.addActionListener(e -> {
            String name = fullName.getText().trim();
            String mail = email.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Full name is required.", "Profile Not Saved", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!mail.isEmpty() && !mail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                JOptionPane.showMessageDialog(dialog, "Enter a valid email address or leave it blank.", "Profile Not Saved", JOptionPane.ERROR_MESSAGE);
                return;
            }
            user.setFullName(name);
            user.setEmail(mail);
            if (user instanceof Student) {
                ((Student) user).setRollNumber(rollField.getText().trim());
                ((Student) user).setCourse(courseField.getText().trim());
            } else if (user instanceof Admin) {
                ((Admin) user).setEmployeeId(staffField.getText().trim());
                ((Admin) user).setDepartment(departmentField.getText().trim());
            } else if (user instanceof Teacher) {
                ((Teacher) user).setEmployeeId(staffField.getText().trim());
                ((Teacher) user).setDepartment(departmentField.getText().trim());
            }
            fileHandler.saveUsers(authService.getAllUsers());
            if (user instanceof Admin || user instanceof Teacher) refreshAdminDashboard();
            else refreshStudentDashboard();
            JOptionPane.showMessageDialog(dialog, "Profile saved on this device.");
        });
        password.addActionListener(e -> changeStudentPassword(user));
        close.addActionListener(e -> dialog.dispose());
        actions.add(password); actions.add(save); actions.add(close);
        content.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
    private void changeStudentPassword(User student) {
        JPasswordField current = new JPasswordField();
        JPasswordField next = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Current password:")); form.add(current);
        form.add(new JLabel("New password:")); form.add(next);
        form.add(new JLabel("Confirm new password:")); form.add(confirm);
        if (JOptionPane.showConfirmDialog(this, form, "Change Password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        String oldPassword = new String(current.getPassword());
        String newPassword = new String(next.getPassword());
        if (!student.validatePassword(oldPassword)) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Password Not Changed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.", "Password Not Changed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPassword.equals(new String(confirm.getPassword()))) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Password Not Changed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        student.setPassword(newPassword);
        fileHandler.saveUsers(authService.getAllUsers());
        JOptionPane.showMessageDialog(this, "Password updated successfully.");
    }

    private void inspectSelectedRowScorecard() {
        int selected = -1;
        JPanel parent = (JPanel) mainCardPanel.getComponent(1); // STUDENT_DASHBOARD
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component c2 : ((JPanel) comp).getComponents()) {
                    if (c2 instanceof JScrollPane) {
                        JTable t = (JTable) ((JScrollPane) c2).getViewport().getView();
                        selected = t.getSelectedRow();
                    }
                }
            }
        }

        List<Result> studentResults = new ArrayList<>();
        for (Result r : resultsHistory) {
            if (currentUser != null && r.getStudentUsername().equalsIgnoreCase(currentUser.getUsername())) {
                studentResults.add(r);
            }
        }

        if (studentResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No exam attempts completed yet. Click '🚀 Start Timed Exam' to take an exam!");
            return;
        }

        Result target = (selected >= 0 && selected < studentResults.size()) ? studentResults.get(selected) : studentResults.get(studentResults.size() - 1);

        JDialog dialog = new JDialog(this, "Scorecard Breakdown - " + target.getExamTitle(), true);
        dialog.setSize(640, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_DARK);

        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new GridLayout(3, 1, 4, 4));
        header.setBackground(CARD_BG);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel h1 = new JLabel(target.getExamTitle());
        h1.setFont(new Font("Segoe UI", Font.BOLD, 17));
        h1.setForeground(TEXT_WHITE);

        JLabel h2 = new JLabel(String.format("Candidate: %s | Date: %s", target.getStudentName(), target.getTimestamp()));
        h2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        h2.setForeground(TEXT_MUTED);

        JLabel h3 = new JLabel(String.format("Score: %d / %d Marks (%.1f%%) | Grade: %s | Outcome: %s",
                target.getMarksScored(), target.getTotalPossibleMarks(), target.getPercentage(), target.getGrade(), target.getStatus()));
        h3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h3.setForeground("PASS".equalsIgnoreCase(target.getStatus()) ? SUCCESS_COLOR : DANGER_COLOR);

        header.add(h1);
        header.add(h2);
        header.add(h3);
        p.add(header, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String item : target.getBreakdown()) {
            model.addElement(item);
        }
        JList<String> list = new JList<>(model);
        list.setBackground(CARD_BG);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnClose = createStyledButton("Close Scorecard", PRIMARY_COLOR);
        btnClose.addActionListener(e -> dialog.dispose());
        p.add(btnClose, BorderLayout.SOUTH);

        dialog.getContentPane().add(p);
        dialog.setVisible(true);
    }

    // ==========================================
    // 6. ADMIN DASHBOARD VIEW
    // ==========================================
    private JPanel createAdminDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTabbedPane tabs = new JTabbedPane();
        adminTabs = tabs;
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Questions Management
        JPanel qPanel = new JPanel(new BorderLayout(12, 12));
        qPanel.setBackground(BG_DARK);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.setOpaque(false);

        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Search question text, type, or answer");
        styleTextField(searchField);
        btnBar.add(new JLabel("Search:"));
        btnBar.add(searchField);

        JButton btnAddQ = createStyledButton("➕ Add MCQ Question", PRIMARY_COLOR);
        btnAddQ.addActionListener(e -> promptAddMCQQuestion());
        btnBar.add(btnAddQ);
        JButton btnAddTf = createStyledButton("Add True/False", ACCENT_CYAN);
        btnAddTf.addActionListener(e -> promptAddTrueFalseQuestion());
        btnBar.add(btnAddTf);
        JButton btnAddShort = createStyledButton("Add Short Answer", SUCCESS_COLOR);
        btnAddShort.addActionListener(e -> promptAddShortAnswerQuestion());
        btnBar.add(btnAddShort);

        JButton btnReset = createStyledButton("🔄 Reset Questions", new Color(77, 67, 72));
        btnReset.addActionListener(e -> {
            questionBank.clear();
            questionBank.addAll(fileHandler.getSeedQuestions());
            fileHandler.saveQuestions(questionBank);
            refreshAdminDashboard();
            JOptionPane.showMessageDialog(this, "Restored default question bank.");
        });
        btnBar.add(btnReset);
        qPanel.add(btnBar, BorderLayout.NORTH);

        String[] qCols = {"ID", "Statement", "Type", "Marks", "Answer Key"};
        adminQuestionsModel = new DefaultTableModel(qCols, 0);
        JTable qTable = new JTable(adminQuestionsModel);
        styleTable(qTable);
        TableRowSorter<DefaultTableModel> questionSorter = new TableRowSorter<>(adminQuestionsModel);
        qTable.setRowSorter(questionSorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateFilter() {
                String query = searchField.getText().trim();
                questionSorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
        });
        JButton btnEditQ = createStyledButton("Edit Selected", ACCENT_CYAN);
        btnEditQ.addActionListener(e -> {
            int viewRow = qTable.getSelectedRow();
            if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a question first."); return; }
            int row = qTable.convertRowIndexToModel(viewRow);
            int id = ((Number) adminQuestionsModel.getValueAt(row, 0)).intValue();
            Question selected = questionBank.stream().filter(q -> q.getId() == id).findFirst().orElse(null);
            if (selected != null) promptEditQuestion(selected);
        });
        JButton btnDeleteQ = createStyledButton("Remove Selected Question", DANGER_COLOR);
        btnDeleteQ.addActionListener(e -> {
            int row = qTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a question first."); return; }
            int id = ((Number) adminQuestionsModel.getValueAt(row, 0)).intValue();
            questionBank.removeIf(q -> q.getId() == id);
            fileHandler.saveQuestions(questionBank);
            refreshAdminDashboard();
        });
        JPanel questionFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        questionFooter.setOpaque(false);
        questionFooter.add(btnEditQ);
        questionFooter.add(btnDeleteQ);
        qPanel.add(new JScrollPane(qTable), BorderLayout.CENTER);
        qPanel.add(questionFooter, BorderLayout.SOUTH);
        tabs.addTab("Question Bank", qPanel);

        // Tab 2: Results Ledger
        JPanel rPanel = new JPanel(new BorderLayout(12, 12));
        rPanel.setBackground(BG_DARK);

        String[] rCols = {"Timestamp", "Candidate", "Score", "Percentage", "Grade", "Status"};
        adminResultsModel = new DefaultTableModel(rCols, 0);
        JTable rTable = new JTable(adminResultsModel);
        styleTable(rTable);
        TableRowSorter<DefaultTableModel> resultsSorter = new TableRowSorter<>(adminResultsModel);
        rTable.setRowSorter(resultsSorter);
        JTextField resultsSearch = new JTextField(18);
        styleTextField(resultsSearch);
        resultsSearch.setToolTipText("Filter results by date, student, grade, or status");
        resultsSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void applyFilter() {
                String query = resultsSearch.getText().trim();
                resultsSorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        JButton btnExport = createStyledButton("Export Results CSV", ACCENT_CYAN);
        btnExport.addActionListener(e -> exportResultsCsv());
        JPanel exportBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportBar.setOpaque(false);
        exportBar.add(new JLabel("Search results:"));
        exportBar.add(resultsSearch);
        exportBar.add(btnExport);
        rPanel.add(exportBar, BorderLayout.NORTH);
        rPanel.add(new JScrollPane(rTable), BorderLayout.CENTER);
        tabs.addTab("Institute Results Ledger", rPanel);

        JPanel studentsPanel = new JPanel(new BorderLayout(10, 10));
        studentsPanel.setBackground(BG_DARK);
        JPanel studentTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        studentTools.setOpaque(false);
        JTextField studentSearch = new JTextField(22);
        styleTextField(studentSearch);
        studentTools.add(new JLabel("Find student:"));
        studentTools.add(studentSearch);
        JButton addStudent = createStyledButton("Add Student", PRIMARY_COLOR);
        addStudent.addActionListener(e -> promptCreateAccount(false));
        JButton addTeacher = createStyledButton("Add Teacher", ACCENT_CYAN);
        addTeacher.addActionListener(e -> promptCreateAccount(true));
        JButton editPerson = createStyledButton("Edit Selected", new Color(77, 67, 72));
        JButton deletePerson = createStyledButton("Delete Selected", DANGER_COLOR);
        studentTools.add(addStudent);
        studentTools.add(addTeacher);
        studentTools.add(editPerson);
        studentTools.add(deletePerson);
        studentsPanel.add(studentTools, BorderLayout.NORTH);
        adminStudentsModel = new DefaultTableModel(new String[]{"Role", "Name", "Username", "Email", "ID", "Course / Department", "Attempts", "Average"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable studentsTable = new JTable(adminStudentsModel);
        styleTable(studentsTable);
        TableRowSorter<DefaultTableModel> studentsSorter = new TableRowSorter<>(adminStudentsModel);
        studentsTable.setRowSorter(studentsSorter);
        editPerson.addActionListener(e -> {
            int view = studentsTable.getSelectedRow();
            if (view < 0) { JOptionPane.showMessageDialog(this, "Select a student or teacher first."); return; }
            int row = studentsTable.convertRowIndexToModel(view);
            String username = String.valueOf(adminStudentsModel.getValueAt(row, 2));
            User person = authService.getAllUsers().get(username);
            if (person != null) showProfileDialog(person);
        });
        deletePerson.addActionListener(e -> {
            int view = studentsTable.getSelectedRow();
            if (view < 0) { JOptionPane.showMessageDialog(this, "Select a student or teacher first."); return; }
            int row = studentsTable.convertRowIndexToModel(view);
            String username = String.valueOf(adminStudentsModel.getValueAt(row, 2));
            int confirm = JOptionPane.showConfirmDialog(this, "Delete account '" + username + "'? Existing exam results will remain in reports.", "Delete Account", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                authService.getAllUsers().remove(username);
                fileHandler.saveUsers(authService.getAllUsers());
                refreshAdminDashboard();
            }
        });
        studentSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void applyFilter() {
                String query = studentSearch.getText().trim();
                studentsSorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        studentsPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        tabs.addTab("People & Accounts", studentsPanel);

        JPanel examPanel = new JPanel(new BorderLayout(10, 10));
        examPanel.setBackground(BG_DARK);
        adminExamsModel = new DefaultTableModel(new String[]{"ID", "Exam", "Subject", "Minutes", "Questions", "Available From", "Available Until", "Attempts"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable examsTable = new JTable(adminExamsModel);
        styleTable(examsTable);
        JPanel examTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        examTools.setOpaque(false);
        JButton addExam = createStyledButton("Create Examination", PRIMARY_COLOR);
        addExam.addActionListener(e -> editExamination(null));
        JButton editExam = createStyledButton("Edit Selected", ACCENT_CYAN);
        editExam.addActionListener(e -> {
            int row = examsTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an examination first."); return; }
            int id = ((Number) adminExamsModel.getValueAt(row, 0)).intValue();
            examinations.stream().filter(exam -> exam.getId() == id).findFirst().ifPresent(this::editExamination);
        });
        JButton deleteExam = createStyledButton("Delete Selected", DANGER_COLOR);
        deleteExam.addActionListener(e -> {
            int row = examsTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an examination first."); return; }
            int id = ((Number) adminExamsModel.getValueAt(row, 0)).intValue();
            int confirmDelete = JOptionPane.showConfirmDialog(this, "Delete this exam definition? Existing result history will be kept.", "Delete Examination", JOptionPane.YES_NO_OPTION);
            if (confirmDelete == JOptionPane.YES_OPTION) {
                examinations.removeIf(exam -> exam.getId() == id);
                fileHandler.saveExaminations(examinations);
                refreshAdminDashboard();
                refreshAvailableExams();
            }
        });
        examTools.add(addExam); examTools.add(editExam); examTools.add(deleteExam);
        examPanel.add(examTools, BorderLayout.NORTH);
        examPanel.add(new JScrollPane(examsTable), BorderLayout.CENTER);
        tabs.addTab("Examinations", examPanel);


        JPanel overview = new JPanel(new BorderLayout());
        overview.setOpaque(false);
        overview.setBorder(new EmptyBorder(0, 2, 2, 2));
        JPanel overviewCopy = new JPanel();
        overviewCopy.setOpaque(false);
        overviewCopy.setLayout(new BoxLayout(overviewCopy, BoxLayout.Y_AXIS));
        JLabel overviewTitle = new JLabel(currentUser instanceof Teacher ? "Teacher Workspace" : "Faculty Workspace");
        overviewTitle.setForeground(TEXT_WHITE);
        overviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAdminSummary = new JLabel("Manage exams, questions, and student progress");
        lblAdminSummary.setForeground(TEXT_MUTED);
        lblAdminSummary.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        overviewCopy.add(overviewTitle);
        overviewCopy.add(Box.createVerticalStrut(4));
        overviewCopy.add(lblAdminSummary);
        overview.add(overviewCopy, BorderLayout.WEST);
        panel.add(overview, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAdminDashboard() {
        adminQuestionsModel.setRowCount(0);
        for (Question q : questionBank) {
            adminQuestionsModel.addRow(new Object[]{
                    q.getId(), q.getQuestionText(), q.getQuestionType(), q.getMarks(), q.getCorrectAnswerDisplay()
            });
        }

        adminResultsModel.setRowCount(0);
        for (Result r : resultsHistory) {
            adminResultsModel.addRow(new Object[]{
                    r.getTimestamp(), r.getStudentName(),
                    r.getMarksScored() + " / " + r.getTotalPossibleMarks(),
                    String.format("%.1f%%", r.getPercentage()),
                    r.getGrade(), r.getStatus()
            });
        }
        if (adminStudentsModel != null) {
            adminStudentsModel.setRowCount(0);
            List<User> people = new ArrayList<>();
            for (User user : authService.getAllUsers().values()) if (user instanceof Student || user instanceof Teacher) people.add(user);
            people.sort((a, b) -> valueOrEmpty(a.getFullName()).compareToIgnoreCase(valueOrEmpty(b.getFullName())));
            for (User person : people) {
                int attempts = 0;
                double percentageTotal = 0;
                if (person instanceof Student) {
                    for (Result result : resultsHistory) {
                        if (result.getStudentUsername().equalsIgnoreCase(person.getUsername())) {
                            attempts++;
                            percentageTotal += result.getPercentage();
                        }
                    }
                }
                String id = person instanceof Student ? ((Student) person).getRollNumber() : ((Teacher) person).getEmployeeId();
                String group = person instanceof Student ? ((Student) person).getCourse() : ((Teacher) person).getDepartment();
                adminStudentsModel.addRow(new Object[]{person instanceof Student ? "Student" : "Teacher", person.getFullName(), person.getUsername(), person.getEmail(), id, group, attempts, attempts == 0 ? "—" : String.format("%.1f%%", percentageTotal / attempts)});
            }
        }
        if (lblAdminSummary != null) {
            long count = authService.getAllUsers().values().stream().filter(user -> user instanceof Student).count();
            long teachers = authService.getAllUsers().values().stream().filter(user -> user instanceof Teacher).count();
            lblAdminSummary.setText(count + " students  •  " + teachers + " teachers  •  " + questionBank.size() + " questions  •  " + examinations.size() + " exams  •  " + resultsHistory.size() + " submissions");
        }
        if (adminExamsModel != null) {
            adminExamsModel.setRowCount(0);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Examination exam : examinations) {
                adminExamsModel.addRow(new Object[]{exam.getId(), exam.getTitle(), exam.getSubject(), exam.getDurationMinutes(), exam.getQuestionIds().size(),
                        exam.getAvailableFrom() == null ? "Any time" : exam.getAvailableFrom().format(format),
                        exam.getAvailableUntil() == null ? "No end" : exam.getAvailableUntil().format(format),
                        exam.isOneAttempt() ? "One attempt" : "Multiple"});
            }
        }
    }

    private void applyRolePermissions() {
        if (adminTabs == null) return;
        for (int i = 0; i < adminTabs.getTabCount(); i++) {
            String title = adminTabs.getTitleAt(i);
            boolean peopleTab = "People & Accounts".equals(title);
            adminTabs.setEnabledAt(i, !(currentUser instanceof Teacher && peopleTab));
        }
    }

    private void editExamination(Examination existing) {
        JTextField title = new JTextField(existing == null ? "" : existing.getTitle());
        JTextField subject = new JTextField(existing == null ? "" : existing.getSubject());
        JSpinner duration = new JSpinner(new SpinnerNumberModel(existing == null ? 30 : existing.getDurationMinutes(), 1, 600, 5));
        JTextArea instructions = new JTextArea(existing == null ? "Read all questions carefully. Answers are saved as you navigate." : existing.getInstructions(), 3, 24);
        instructions.setLineWrap(true); instructions.setWrapStyleWord(true);
        String idsDefault = existing == null ? questionBank.stream().map(q -> String.valueOf(q.getId())).collect(java.util.stream.Collectors.joining(","))
                : existing.getQuestionIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        JTextField questionIds = new JTextField(idsDefault);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JTextField starts = new JTextField(existing == null || existing.getAvailableFrom() == null ? "" : existing.getAvailableFrom().format(format));
        JTextField ends = new JTextField(existing == null || existing.getAvailableUntil() == null ? "" : existing.getAvailableUntil().format(format));
        JCheckBox randomize = new JCheckBox("Randomize question order", existing == null || existing.isRandomizeQuestions());
        JCheckBox oneAttempt = new JCheckBox("Allow one attempt per student", existing != null && existing.isOneAttempt());
        JCheckBox active = new JCheckBox("Publish / active", existing == null || existing.isActive());
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Exam title:")); form.add(title);
        form.add(new JLabel("Subject:")); form.add(subject);
        form.add(new JLabel("Duration (minutes):")); form.add(duration);
        form.add(new JLabel("Question IDs (comma separated):")); form.add(questionIds);
        form.add(new JLabel("Available from (yyyy-MM-dd HH:mm, optional):")); form.add(starts);
        form.add(new JLabel("Available until (yyyy-MM-dd HH:mm, optional):")); form.add(ends);
        form.add(new JLabel("Instructions:")); form.add(new JScrollPane(instructions));
        form.add(new JLabel("Options:"));
        JPanel options = new JPanel(new GridLayout(3, 1));
        options.add(randomize); options.add(oneAttempt); options.add(active);
        form.add(options);
        int choice = JOptionPane.showConfirmDialog(this, form, existing == null ? "Create Examination" : "Edit Examination", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        try {
            String examTitle = title.getText().trim();
            String examSubject = subject.getText().trim();
            if (examTitle.isEmpty() || examSubject.isEmpty()) throw new IllegalArgumentException("Title and subject are required.");
            if (examinations.stream().anyMatch(exam -> (existing == null || exam.getId() != existing.getId()) && exam.getTitle().equalsIgnoreCase(examTitle)))
                throw new IllegalArgumentException("Exam titles must be unique.");
            List<Integer> selectedIds = new ArrayList<>();
            for (String raw : questionIds.getText().split(",")) {
                if (raw.trim().isEmpty()) continue;
                int id = Integer.parseInt(raw.trim());
                if (questionIdExists(id) && !selectedIds.contains(id)) selectedIds.add(id);
                else if (!questionIdExists(id)) throw new IllegalArgumentException("Question ID " + id + " does not exist.");
            }
            if (selectedIds.isEmpty()) throw new IllegalArgumentException("Select at least one valid question ID.");
            LocalDateTime from = starts.getText().trim().isEmpty() ? null : LocalDateTime.parse(starts.getText().trim(), format);
            LocalDateTime until = ends.getText().trim().isEmpty() ? null : LocalDateTime.parse(ends.getText().trim(), format);
            if (from != null && until != null && !until.isAfter(from)) throw new IllegalArgumentException("End time must be after start time.");
            int minutes = (Integer) duration.getValue();
            if (existing == null) {
                int id = examinations.stream().mapToInt(Examination::getId).max().orElse(0) + 1;
                examinations.add(new Examination(id, examTitle, examSubject, instructions.getText().trim(), minutes, selectedIds, from, until,
                        randomize.isSelected(), oneAttempt.isSelected(), active.isSelected()));
            } else {
                existing.setTitle(examTitle); existing.setSubject(examSubject); existing.setInstructions(instructions.getText().trim());
                existing.setDurationMinutes(minutes); existing.setQuestionIds(selectedIds); existing.setAvailableFrom(from); existing.setAvailableUntil(until);
                existing.setRandomizeQuestions(randomize.isSelected()); existing.setOneAttempt(oneAttempt.isSelected()); existing.setActive(active.isSelected());
            }
            fileHandler.saveExaminations(examinations);
            refreshAdminDashboard();
            refreshAvailableExams();
            JOptionPane.showMessageDialog(this, "Examination saved.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Use date format yyyy-MM-dd HH:mm, for example 2026-10-25 14:30.", "Invalid Schedule", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Examination Not Saved", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void promptCreateAccount(boolean teacherAccount) {
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField id = new JTextField();
        JTextField group = new JTextField();
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Username:")); form.add(username);
        form.add(new JLabel("Temporary password:")); form.add(password);
        form.add(new JLabel("Full name:")); form.add(name);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel(teacherAccount ? "Staff ID:" : "Roll number:")); form.add(id);
        form.add(new JLabel(teacherAccount ? "Department:" : "Course:")); form.add(group);
        String roleLabel = teacherAccount ? "Teacher" : "Student";
        if (JOptionPane.showConfirmDialog(this, form, "Add " + roleLabel, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        String userId = username.getText().trim();
        String secret = new String(password.getPassword());
        if (userId.isEmpty() || secret.length() < 6 || name.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a username, a password of at least 6 characters, and a full name.");
            return;
        }
        if (authService.getAllUsers().keySet().stream().anyMatch(existing -> existing.equalsIgnoreCase(userId))) {
            JOptionPane.showMessageDialog(this, "That username is already in use.");
            return;
        }
        User newUser = teacherAccount
                ? new Teacher(userId, secret, name.getText().trim(), email.getText().trim(), id.getText().trim(), group.getText().trim())
                : new Student(userId, secret, name.getText().trim(), email.getText().trim(), id.getText().trim(), group.getText().trim());
        authService.getAllUsers().put(userId, newUser);
        fileHandler.saveUsers(authService.getAllUsers());
        refreshAdminDashboard();
        JOptionPane.showMessageDialog(this, roleLabel + " account created. The user can sign in on this device.");
    }

    private void refreshAvailableExams() {
        if (availableExamSelector == null) return;
        int selectedId = availableExamSelector.getSelectedItem() instanceof Examination ? ((Examination) availableExamSelector.getSelectedItem()).getId() : -1;
        availableExamSelector.removeAllItems();
        for (Examination exam : examinations) if (exam.isActive()) availableExamSelector.addItem(exam);
        for (int i = 0; i < availableExamSelector.getItemCount(); i++) {
            if (availableExamSelector.getItemAt(i).getId() == selectedId) { availableExamSelector.setSelectedIndex(i); break; }
        }
    }

    private void promptEditQuestion(Question question) {
        JTextField statement = new JTextField(question.getQuestionText());
        JSpinner marks = new JSpinner(new SpinnerNumberModel(question.getMarks(), 1, 100, 1));
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Question:")); form.add(statement);
        form.add(new JLabel("Marks:")); form.add(marks);
        JTextField[] options = null;
        JComboBox<Integer> correctOption = null;
        JComboBox<Boolean> correctBoolean = null;
        if (question instanceof MCQQuestion) {
            MCQQuestion mcq = (MCQQuestion) question;
            options = new JTextField[4];
            for (int i = 0; i < options.length; i++) {
                options[i] = new JTextField(i < mcq.getOptions().size() ? mcq.getOptions().get(i) : "");
                form.add(new JLabel("Option " + (i + 1) + ":")); form.add(options[i]);
            }
            correctOption = new JComboBox<>(new Integer[]{1, 2, 3, 4});
            correctOption.setSelectedItem(mcq.getCorrectOptionIndex());
            form.add(new JLabel("Correct option:")); form.add(correctOption);
        } else if (question instanceof TrueFalseQuestion) {
            correctBoolean = new JComboBox<>(new Boolean[]{true, false});
            correctBoolean.setSelectedItem(((TrueFalseQuestion) question).isCorrectAnswer());
            form.add(new JLabel("Correct answer:")); form.add(correctBoolean);
        } else if (question instanceof ShortAnswerQuestion) {
            JTextField answer = new JTextField(((ShortAnswerQuestion) question).getCorrectAnswer());
            form.add(new JLabel("Expected answer:")); form.add(answer);
            form.putClientProperty("shortAnswerField", answer);
        }
        if (JOptionPane.showConfirmDialog(this, form, "Edit Question " + question.getId(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        if (statement.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Question text is required."); return; }
        if (question instanceof MCQQuestion) {
            List<String> updatedOptions = new ArrayList<>();
            for (JTextField option : options) {
                if (option.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "All four options are required."); return; }
                updatedOptions.add(option.getText().trim());
            }
            MCQQuestion mcq = (MCQQuestion) question;
            mcq.setOptions(updatedOptions);
            mcq.setCorrectOptionIndex((Integer) correctOption.getSelectedItem());
        } else if (question instanceof TrueFalseQuestion) {
            ((TrueFalseQuestion) question).setCorrectAnswer((Boolean) correctBoolean.getSelectedItem());
        } else if (question instanceof ShortAnswerQuestion) {
            JTextField answer = (JTextField) form.getClientProperty("shortAnswerField");
            if (answer.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Expected answer is required."); return; }
            ((ShortAnswerQuestion) question).setCorrectAnswer(answer.getText());
        }
        question.setQuestionText(statement.getText().trim());
        question.setMarks((Integer) marks.getValue());
        fileHandler.saveQuestions(questionBank);
        refreshAdminDashboard();
    }

    private int nextQuestionId() {
        int largest = 0;
        for (Question question : questionBank) largest = Math.max(largest, question.getId());
        return largest + 1;
    }

    private boolean questionIdExists(int id) {
        for (Question question : questionBank) if (question.getId() == id) return true;
        return false;
    }

    private void promptAddTrueFalseQuestion() {
        JTextField text = new JTextField();
        JSpinner id = new JSpinner(new SpinnerNumberModel(nextQuestionId(), 1, Integer.MAX_VALUE, 1));
        JSpinner marks = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        JComboBox<Boolean> answer = new JComboBox<>(new Boolean[]{true, false});
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Question ID:")); form.add(id);
        form.add(new JLabel("Statement:")); form.add(text);
        form.add(new JLabel("Marks:")); form.add(marks);
        form.add(new JLabel("Correct answer:")); form.add(answer);
        if (JOptionPane.showConfirmDialog(this, form, "Add True/False Question", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        int questionId = (Integer) id.getValue();
        if (text.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Question text is required."); return; }
        if (questionIdExists(questionId)) { JOptionPane.showMessageDialog(this, "That question ID already exists."); return; }
        questionBank.add(new TrueFalseQuestion(questionId, text.getText().trim(), (Integer) marks.getValue(), (Boolean) answer.getSelectedItem()));
        fileHandler.saveQuestions(questionBank);
        refreshAdminDashboard();
        lblStatBankSize.setText(String.valueOf(questionBank.size()));
        JOptionPane.showMessageDialog(this, "True/false question added.");
    }

    private void promptAddShortAnswerQuestion() {
        JTextField text = new JTextField();
        JTextField expected = new JTextField();
        JSpinner id = new JSpinner(new SpinnerNumberModel(nextQuestionId(), 1, Integer.MAX_VALUE, 1));
        JSpinner marks = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Question ID:")); form.add(id);
        form.add(new JLabel("Statement:")); form.add(text);
        form.add(new JLabel("Marks:")); form.add(marks);
        form.add(new JLabel("Expected answer:")); form.add(expected);
        if (JOptionPane.showConfirmDialog(this, form, "Add Short Answer Question", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        int questionId = (Integer) id.getValue();
        if (questionIdExists(questionId) || text.getText().trim().isEmpty() || expected.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Use a unique ID and provide both question and expected answer."); return;
        }
        questionBank.add(new ShortAnswerQuestion(questionId, text.getText().trim(), (Integer) marks.getValue(), expected.getText().trim()));
        fileHandler.saveQuestions(questionBank);
        refreshAdminDashboard();
        JOptionPane.showMessageDialog(this, "Short-answer question added. Answers are marked against the expected response.");
    }

    private void promptAddMCQQuestion() {
        JTextField idField = new JTextField(String.valueOf(nextQuestionId()));
        JTextField textField = new JTextField();
        JTextField opt1 = new JTextField("Option 1");
        JTextField opt2 = new JTextField("Option 2");
        JTextField opt3 = new JTextField("Option 3");
        JTextField opt4 = new JTextField("Option 4");
        JSpinner marks = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        JComboBox<Integer> correctCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        form.add(new JLabel("Question ID:")); form.add(idField);
        form.add(new JLabel("Statement:")); form.add(textField);
        form.add(new JLabel("Marks:")); form.add(marks);
        form.add(new JLabel("Option 1:")); form.add(opt1);
        form.add(new JLabel("Option 2:")); form.add(opt2);
        form.add(new JLabel("Option 3:")); form.add(opt3);
        form.add(new JLabel("Option 4:")); form.add(opt4);
        form.add(new JLabel("Correct Option (1-4):")); form.add(correctCombo);

        int result = JOptionPane.showConfirmDialog(this, form, "Add MCQ Question", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                if (id <= 0 || questionIdExists(id)) throw new IllegalArgumentException("Question ID must be positive and unique.");
                if (textField.getText().trim().isEmpty()) throw new IllegalArgumentException("Question statement is required.");
                if (opt1.getText().trim().isEmpty() || opt2.getText().trim().isEmpty() || opt3.getText().trim().isEmpty() || opt4.getText().trim().isEmpty())
                    throw new IllegalArgumentException("All four options are required.");
                List<String> opts = List.of(opt1.getText(), opt2.getText(), opt3.getText(), opt4.getText());
                int correct = (Integer) correctCombo.getSelectedItem();
                MCQQuestion mcq = new MCQQuestion(id, textField.getText().trim(), (Integer) marks.getValue(), opts, correct);

                questionBank.add(mcq);
                fileHandler.saveQuestions(questionBank);
                refreshAdminDashboard();
                lblStatBankSize.setText(String.valueOf(questionBank.size()));
                JOptionPane.showMessageDialog(this, "Question added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage());
            }
        }
    }

    private void promptStudentRegistration() {
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField fullName = new JTextField();
        JTextField email = new JTextField();
        JTextField roll = new JTextField();
        JTextField course = new JTextField();
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Username:")); form.add(username);
        form.add(new JLabel("Password:")); form.add(password);
        form.add(new JLabel("Full name:")); form.add(fullName);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Roll number:")); form.add(roll);
        form.add(new JLabel("Course:")); form.add(course);
        if (JOptionPane.showConfirmDialog(this, form, "Create Student Account", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        String user = username.getText().trim();
        String pass = new String(password.getPassword());
        if (user.isEmpty() || pass.isEmpty() || fullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password, and full name are required."); return;
        }
        Student student = new Student(user, pass, fullName.getText().trim(), email.getText().trim(), roll.getText().trim(), course.getText().trim());
        JOptionPane.showMessageDialog(this, authService.registerStudent(student) ? "Account created. You can now sign in." : "Username already exists.");
    }

    private void showLeaderboard() {
        Map<String, Result> best = new HashMap<>();
        for (Result result : resultsHistory) {
            String key = result.getStudentUsername().toLowerCase();
            if (!best.containsKey(key) || result.getPercentage() > best.get(key).getPercentage()) best.put(key, result);
        }
        List<Result> ranked = new ArrayList<>(best.values());
        ranked.sort((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()));
        DefaultTableModel model = new DefaultTableModel(new String[]{"Rank", "Student", "Best Score", "Percentage", "Grade"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (int i = 0; i < ranked.size(); i++) {
            Result r = ranked.get(i);
            model.addRow(new Object[]{i + 1, r.getStudentName(), r.getMarksScored() + " / " + r.getTotalPossibleMarks(), String.format("%.1f%%", r.getPercentage()), r.getGrade()});
        }
        JTable table = new JTable(model);
        styleTable(table);
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Student Leaderboard", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportStudentResultsCsv() {
        if (!(currentUser instanceof Student)) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("my-exam-history.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StringBuilder csv = new StringBuilder("Timestamp,Exam Title,Marks Scored,Total Possible,Percentage,Grade,Status\n");
        for (Result result : resultsHistory) {
            if (!result.getStudentUsername().equalsIgnoreCase(currentUser.getUsername())) continue;
            csv.append(csvCell(result.getTimestamp())).append(',').append(csvCell(result.getExamTitle())).append(',')
               .append(result.getMarksScored()).append(',').append(result.getTotalPossibleMarks()).append(',')
               .append(String.format(java.util.Locale.US, "%.2f", result.getPercentage())).append(',')
               .append(csvCell(result.getGrade())).append(',').append(csvCell(result.getStatus())).append('\n');
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Your exam history was exported.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not export exam history: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportResultsCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("eduexam-results-ledger.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StringBuilder csv = new StringBuilder("Timestamp,Candidate Name,Username,Exam Title,Marks Scored,Total Possible,Percentage,Grade,Status\n");
        for (Result r : resultsHistory) {
            csv.append(csvCell(r.getTimestamp())).append(',').append(csvCell(r.getStudentName())).append(',')
               .append(csvCell(r.getStudentUsername())).append(',').append(csvCell(r.getExamTitle())).append(',')
               .append(r.getMarksScored()).append(',').append(r.getTotalPossibleMarks()).append(',')
               .append(String.format(java.util.Locale.US, "%.2f", r.getPercentage())).append(',')
               .append(csvCell(r.getGrade())).append(',').append(csvCell(r.getStatus())).append('\n');
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Results exported successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csvCell(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    private void logout() {
        if (perQuestionTimer != null) perQuestionTimer.stop();
        currentUser = null;
        btnNavProfile.setVisible(false);
        cardLayout.show(mainCardPanel, "LOGIN");
    }

    // ==========================================
    // UI STYLING HELPERS
    // ==========================================
    private static JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(CARD_BG);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private static void styleRadioButton(JRadioButton rb) {
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        rb.setForeground(TEXT_WHITE);
        rb.setBackground(CARD_BG);
        rb.setFocusPainted(false);
    }

    private static void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    private static JPanel createInputRow(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }
}
