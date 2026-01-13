package com.university.erp.admin;

public class AddUserResult {
    private final boolean success;
    private final Integer userId;
    private final String message;


    public AddUserResult(boolean success, Integer userId, String message) {
        this.success = success;
        this.userId = userId;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }
}