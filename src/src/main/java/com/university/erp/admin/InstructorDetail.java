package com.university.erp.admin;


public class InstructorDetail {
    private final int userId;
    private final String username;
    private final String status;
    private final String department;

    public InstructorDetail(int userId, String username, String status, String department) {
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.department = department;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public String getDepartment() { return department; }
}
