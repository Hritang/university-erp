
package com.university.erp.domain;

import java.sql.Timestamp;
import java.util.Objects;


public class UserRecord {
    private final int userId;
    private final String username;
    private final String role;
    private final String passwordHash;
    private final String status;
    private final Timestamp lastLogin;


    private final String rollNo;
    private final String program;
    private final Integer year;
    private final String department;


    public UserRecord(int userId, String username, String role,
                      String passwordHash, String status, Timestamp lastLogin,
                      String rollNo, String program, Integer year, String department) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.status = status;
        this.lastLogin = lastLogin;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
        this.department = department;
    }


    public UserRecord(int userId, String username, String role,
                      String passwordHash, String status, Timestamp lastLogin) {
        this(userId, username, role, passwordHash, status, lastLogin, null, null, null, null);
    }


    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
    public String getStatus() { return status; }
    public Timestamp getLastLogin() { return lastLogin; }


    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public Integer getYear() { return year; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRecord)) return false;
        UserRecord that = (UserRecord) o;
        return userId == that.userId && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}
