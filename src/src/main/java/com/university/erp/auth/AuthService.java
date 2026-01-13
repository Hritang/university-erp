package com.university.erp.auth;

import com.university.erp.domain.UserRecord;
import com.university.erp.util.PasswordUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService() { this.authDAO = new AuthDAO(); }

    public AuthService(AuthDAO dao) { this.authDAO = dao; }

    public boolean authenticate(String username, String password) {
        AuthResult r = authenticateWithResult(username, password);
        return r != null && r.isSuccess();
    }


    public AuthResult authenticateWithResult(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return AuthResult.failInput();
        }

        try {
            Optional<UserRecord> optUser = authDAO.findByUsername(username.trim());
            if (optUser.isEmpty()) return AuthResult.failNotFound();

            UserRecord user = optUser.get();
            if (!"active".equalsIgnoreCase(String.valueOf(user.getStatus()))) {
                return AuthResult.failInactive();
            }

            String dbHash = user.getPasswordHash();
            if (dbHash == null || dbHash.isBlank()) return AuthResult.error("Missing hash");

            boolean ok = PasswordUtil.checkPassword(password, dbHash);
            if (!ok) return AuthResult.failBadCreds();


            try {
                authDAO.updateLastLogin(user.getUserId(), Timestamp.from(Instant.now()));
            } catch (Exception ex) {

            }

            return AuthResult.success(user.getRole(), user.getUserId());
        } catch (Exception ex) {
            return AuthResult.error("Unexpected: " + ex.getMessage());
        }
    }
}
