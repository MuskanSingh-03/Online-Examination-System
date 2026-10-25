package com.exam.system.model;

/**
 * Models a Faculty/Administrator managing the exam platform.
 * Inherits from User and specializes administrative actions.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String department;

    public Admin(String username, String password, String fullName, String email, String employeeId, String department) {
        super(username, password, fullName, email, "ADMIN");
        this.employeeId = employeeId;
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n==================================================");
        System.out.println("                 ADMIN DASHBOARD                  ");
        System.out.println("==================================================");
        System.out.println("Welcome, " + getFullName() + " [Staff ID: " + employeeId + "]");
        System.out.println("Department: " + department);
        System.out.println("--------------------------------------------------");
        System.out.println("1. View All Questions in Bank");
        System.out.println("2. Add Multiple Choice Question (MCQ)");
        System.out.println("3. Add True / False Question");
        System.out.println("4. Delete Question by ID");
        System.out.println("5. View All Student Exam Results (Aggregate)");
        System.out.println("6. Reset / Reload Default Data Bank");
        System.out.println("7. View My Profile");
        System.out.println("8. Logout");
        System.out.println("==================================================");
    }
}
