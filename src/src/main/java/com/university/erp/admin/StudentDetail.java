package com.university.erp.admin;

public class StudentDetail {
    private final int userId;
    private final String username;
    private final String status;
    private final String rollNo;
    private final String program;
    private final Integer year;

    public StudentDetail(int userId, String username, String status,
                         String rollNo, String program, Integer year) {
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public Integer getYear() { return year; }
}
