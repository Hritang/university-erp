package com.university.erp.auth;

public class AuthResult {
    public enum Status { SUCCESS, FAIL_INPUT, FAIL_NOT_FOUND, FAIL_INACTIVE, FAIL_BAD_CREDENTIALS, ERROR }

    private final Status status;
    private final String role;
    private final int userId;
    private final String message;

    public AuthResult(Status status, String role, int userId, String message) {
        this.status = status;
        this.role = role;
        this.userId = userId;
        this.message = message;
    }

    public static AuthResult success(String role, int userId) {
        return new AuthResult(Status.SUCCESS, role, userId, "OK");
    }
    public static AuthResult failInput() {
        return new AuthResult(Status.FAIL_INPUT, null, -1, "Missing username or password");
    }
    public static AuthResult failNotFound() {
        return new AuthResult(Status.FAIL_NOT_FOUND, null, -1, "User not found");
    }
    public static AuthResult failInactive() {
        return new AuthResult(Status.FAIL_INACTIVE, null, -1, "Account not active");
    }
    public static AuthResult failBadCreds() {
        return new AuthResult(Status.FAIL_BAD_CREDENTIALS, null, -1, "Invalid credentials");
    }
    public static AuthResult error(String msg) {
        return new AuthResult(Status.ERROR, null, -1, msg);
    }

    public Status getStatus() { return status; }
    public String getRole() { return role; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
}
