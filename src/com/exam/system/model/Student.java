package com.exam.system.model;

/**
 * Models a Student taking examinations.
 * Inherits from User and specializes dashboard and attributes.
 */
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String rollNumber;
    private String course;

    public Student(String username, String password, String fullName, String email, String rollNumber, String course) {
        super(username, password, fullName, email, "STUDENT");
        this.rollNumber = rollNumber;
        this.course = course;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n==================================================");
        System.out.println("                 STUDENT DASHBOARD                ");
        System.out.println("==================================================");
        System.out.println("Welcome, " + getFullName() + " | Roll No: " + rollNumber);
        System.out.println("Program: " + course);
        System.out.println("--------------------------------------------------");
        System.out.println("1. Start Timed Examination Session");
        System.out.println("2. View My Score Cards & Exam History");
        System.out.println("3. View My Profile");
        System.out.println("4. Logout");
        System.out.println("==================================================");
    }
}
