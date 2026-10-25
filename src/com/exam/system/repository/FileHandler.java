package com.exam.system.repository;

import com.exam.system.model.Admin;
import com.exam.system.model.MCQQuestion;
import com.exam.system.model.Question;
import com.exam.system.model.Result;
import com.exam.system.model.Student;
import com.exam.system.model.TrueFalseQuestion;
import com.exam.system.model.Examination;
import com.exam.system.model.Teacher;
import com.exam.system.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence layer implementing Java Object Serialization.
 * Saves and loads users, questions, and test results to the data/ directory,
 * ensuring system state survives application reboots.
 */
public class FileHandler {
    private static File baseDir;

    public static File getBaseDir() {
        if (baseDir != null) {
            return baseDir;
        }
        try {
            File codeSource = new File(FileHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (codeSource.isFile()) {
                baseDir = codeSource.getParentFile();
            } else if (codeSource.isDirectory()) {
                if (codeSource.getName().equalsIgnoreCase("bin")) {
                    baseDir = codeSource.getParentFile();
                } else {
                    baseDir = codeSource;
                }
            }
        } catch (Exception e) {
            baseDir = new File(System.getProperty("user.dir"));
        }
        if (baseDir == null || !baseDir.exists()) {
            baseDir = new File("c:\\Users\\Asus\\Desktop\\online_examination_system");
        }
        return baseDir;
    }

    public static File getDataDir() {
        File dir = new File(getBaseDir(), "data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getUsersFile() {
        return new File(getDataDir(), "users.dat");
    }

    public static File getQuestionsFile() {
        return new File(getDataDir(), "questions.dat");
    }

    public static File getResultsFile() {
        return new File(getDataDir(), "results.dat");
    }

    public static File getExaminationsFile() {
        return new File(getDataDir(), "examinations.dat");
    }

    public static String getUsersFilePath() {
        return getUsersFile().getAbsolutePath();
    }

    public static String getQuestionsFilePath() {
        return getQuestionsFile().getAbsolutePath();
    }

    public static String getResultsFilePath() {
        return getResultsFile().getAbsolutePath();
    }

    public FileHandler() {
        getDataDir();
    }

    // ==========================================
    // USER PERSISTENCE
    // ==========================================
    public synchronized void saveUsers(Map<String, User> users) {
        saveObject(getUsersFilePath(), users);
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String, User> loadUsers() {
        File file = getUsersFile();
        if (!file.exists()) {
            Map<String, User> initialUsers = getSeedUsers();
            saveUsers(initialUsers);
            return initialUsers;
        }
        Object obj = loadObject(getUsersFilePath());
        if (obj instanceof Map) {
            Map<String, User> users = (Map<String, User>) obj;
            if (!users.containsKey("teacher1")) {
                Teacher teacher = new Teacher("teacher1", "teach123", "Arjun Patel", "arjun.patel@college.edu", "TCH-104", "Computer Science");
                users.put(teacher.getUsername(), teacher);
                saveUsers(users);
            }
            return users;
        }
        Map<String, User> fallback = getSeedUsers();
        saveUsers(fallback);
        return fallback;
    }

    private Map<String, User> getSeedUsers() {
        Map<String, User> map = new HashMap<>();
        // Pre-seeded Student
        Student s1 = new Student(
                "student1",
                "pass123",
                "Rahul Sharma",
                "rahul.sharma@college.edu",
                "CS2026-042",
                "B.Tech Computer Science"
        );
        map.put(s1.getUsername(), s1);

        // Pre-seeded Admin
        Admin a1 = new Admin(
                "admin1",
                "admin123",
                "Dr. Priya Mehta",
                "priya.mehta@college.edu",
                "FAC-901",
                "Computer Science & Engineering"
        );
        map.put(a1.getUsername(), a1);
        Teacher t1 = new Teacher("teacher1", "teach123", "Arjun Patel", "arjun.patel@college.edu", "TCH-104", "Computer Science");
        map.put(t1.getUsername(), t1);

        return map;
    }

    // ==========================================
    // QUESTION BANK PERSISTENCE
    // ==========================================
    public synchronized void saveQuestions(List<Question> questions) {
        saveObject(getQuestionsFilePath(), new ArrayList<>(questions));
    }

    @SuppressWarnings("unchecked")
    public synchronized List<Question> loadQuestions() {
        File file = getQuestionsFile();
        if (!file.exists()) {
            List<Question> initial = getSeedQuestions();
            saveQuestions(initial);
            return initial;
        }
        Object obj = loadObject(getQuestionsFilePath());
        if (obj instanceof List) {
            return (List<Question>) obj;
        }
        List<Question> fallback = getSeedQuestions();
        saveQuestions(fallback);
        return fallback;
    }

    public List<Question> getSeedQuestions() {
        List<Question> seed = new ArrayList<>();

        // Question 1 (MCQ - Polymorphism)
        seed.add(new MCQQuestion(
                1,
                "Which OOP concept enables a child class to provide a specific implementation of a parent class method?",
                5,
                Arrays.asList("Encapsulation", "Method Overriding (Polymorphism)", "Method Overloading", "Abstraction"),
                2
        ));

        // Question 2 (MCQ - Collections)
        seed.add(new MCQQuestion(
                2,
                "Which Java collection stores key-value pairs with O(1) average lookup time?",
                5,
                Arrays.asList("ArrayList", "LinkedList", "HashMap", "TreeSet"),
                3
        ));

        // Question 3 (MCQ - Exception Handling)
        seed.add(new MCQQuestion(
                3,
                "Which keyword is used in Java to explicitly throw an exception?",
                5,
                Arrays.asList("throws", "throw", "catch", "try"),
                2
        ));

        // Question 4 (True/False - Abstraction)
        seed.add(new TrueFalseQuestion(
                4,
                "An abstract class in Java can contain both abstract and concrete methods.",
                5,
                true
        ));

        // Question 5 (True/False - Inheritance)
        seed.add(new TrueFalseQuestion(
                5,
                "Java supports multiple inheritance for classes using the 'extends' keyword.",
                5,
                false
        ));

        // Question 6 (MCQ - Collections)
        seed.add(new MCQQuestion(
                6,
                "What is the default initial capacity of an ArrayList in standard Java?",
                5,
                Arrays.asList("5", "10", "16", "32"),
                2
        ));

        // Question 7 (MCQ - Multithreading)
        seed.add(new MCQQuestion(
                7,
                "Which interface must a class implement to enable its code to be executed by a Thread instance?",
                5,
                Arrays.asList("Callable", "Runnable", "Cloneable", "Serializable"),
                2
        ));

        // Question 8 (True/False - Encapsulation)
        seed.add(new TrueFalseQuestion(
                8,
                "Marking class fields private and providing public getters and setters is a core demonstration of Encapsulation.",
                5,
                true
        ));

        // Question 9 (MCQ - Memory Management)
        seed.add(new MCQQuestion(
                9,
                "Where are Java object instances dynamically allocated in memory at runtime?",
                5,
                Arrays.asList("Call Stack", "Heap Memory", "Registers", "Method Area"),
                2
        ));

        // Question 10 (True/False - Final keyword)
        seed.add(new TrueFalseQuestion(
                10,
                "In Java, a method declared with the 'final' keyword can be overridden in a subclass.",
                5,
                false
        ));

        return seed;
    }

    public synchronized void saveExaminations(List<Examination> exams) {
        saveObject(getExaminationsFile().getAbsolutePath(), new ArrayList<>(exams));
    }

    @SuppressWarnings("unchecked")
    public synchronized List<Examination> loadExaminations() {
        File file = getExaminationsFile();
        if (!file.exists()) {
            List<Examination> seed = getSeedExaminations();
            saveExaminations(seed);
            return seed;
        }
        Object stored = loadObject(file.getAbsolutePath());
        if (stored instanceof List) return (List<Examination>) stored;
        List<Examination> seed = getSeedExaminations();
        saveExaminations(seed);
        return seed;
    }

    private List<Examination> getSeedExaminations() {
        return new ArrayList<>(Arrays.asList(new Examination(
                1, "Java Fundamentals Assessment", "Java",
                "Read each question carefully. You have 30 minutes. Your answers are saved as you move through the exam. The exam submits automatically when time expires.",
                30, Arrays.asList(1,2,3,4,5,6,7,8,9,10), null, null, true, false, true
        )));
    }

    // ==========================================
    // RESULT PERSISTENCE
    // ==========================================
    public synchronized void saveResults(List<Result> results) {
        saveObject(getResultsFilePath(), new ArrayList<>(results));
    }

    @SuppressWarnings("unchecked")
    public synchronized List<Result> loadResults() {
        File file = getResultsFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }
        Object obj = loadObject(getResultsFilePath());
        if (obj instanceof List) {
            return (List<Result>) obj;
        }
        return new ArrayList<>();
    }

    // ==========================================
    // LOW-LEVEL SERIALIZATION HELPERS
    // ==========================================
    private void saveObject(String filePath, Object object) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(object);
        } catch (IOException e) {
            System.err.println("[PERSISTENCE WARNING] Could not write to " + filePath + ": " + e.getMessage());
        }
    }

    private Object loadObject(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[PERSISTENCE WARNING] Failed reading " + filePath + " (" + e.getMessage() + "). Initializing fallback.");
            return null;
        }
    }
}
