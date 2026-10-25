package com.exam.system.model;

/** Teacher account with examination and submission review permissions. */
public class Teacher extends User {
    private static final long serialVersionUID = 1L;
    private String employeeId;
    private String department;

    public Teacher(String username, String password, String fullName, String email, String employeeId, String department) {
        super(username, password, fullName, email, "TEACHER");
        this.employeeId = employeeId;
        this.department = department;
    }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String value) { employeeId = value; }
    public String getDepartment() { return department; }
    public void setDepartment(String value) { department = value; }
    @Override public void displayDashboard() {
        System.out.println("Teacher dashboard: " + getFullName() + " (" + employeeId + ")");
    }
}
