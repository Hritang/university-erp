package com.university.erp.ui.controllers;

import com.university.erp.auth.AuthResult;
import com.university.erp.auth.AuthService;
import com.university.erp.data.DBConnection;
import com.university.erp.domain.UserRecord;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class AuthController {
    private final AuthService authService;

    public AuthController() { this.authService = new AuthService(); }
    public AuthController(AuthService service) { this.authService = service; }


    public String login(String username, String password) {
        AuthResult r = authService.authenticateWithResult(username, password);
        if (r == null) return "ERROR:NullResult";

        if (r.isSuccess()) {
            return "SUCCESS:" + (r.getRole() == null ? "unknown" : r.getRole());
        }

        switch (r.getStatus()) {
            case FAIL_INPUT: return "FAIL_INPUT";
            case FAIL_NOT_FOUND: return "FAIL_BAD_CRED";
            case FAIL_INACTIVE: return "FAIL_INACTIVE";
            case FAIL_BAD_CREDENTIALS: return "FAIL_BAD_CRED";
            case ERROR: return "ERROR:" + r.getMessage();
            default: return "ERROR:unknown";
        }
    }
    public boolean changePassword(String username, String newPlainPassword) {
        // 1. Hash the new password
        String newHash = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt(10));

        // 2. Call the DB (This code moves here from the Frame)
        try (Connection conn = DBConnection.getConnection(); // Make sure this connects to Auth DB
             PreparedStatement ps = conn.prepareStatement("UPDATE users_auth SET password_hash = ? WHERE username = ?")) {

            ps.setString(1, newHash);
            ps.setString(2, username);
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
