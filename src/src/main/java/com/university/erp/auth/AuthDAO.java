package com.university.erp.auth;

import com.university.erp.data.DBConnection;
import com.university.erp.domain.UserRecord;

import java.sql.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;


public class AuthDAO {


//    Finds a user by their username.
    public Optional<UserRecord> findByUsername(String username) {
        String query = "SELECT user_id, username, role, password_hash, status, last_login FROM users_auth WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserRecord user = new UserRecord(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("password_hash"),
                            rs.getString("status"),
                            rs.getTimestamp("last_login")
                    );
                    return Optional.of(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

//     Updates the last login timestamp for a user.

    public void updateLastLogin(int userId, Timestamp ts) {
        String updateQuery = "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setTimestamp(1, ts);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    Returns all users (basic fields) -- used by admin dashboard.

    public List<UserRecord> listAllUsers() {
        List<UserRecord> out = new ArrayList<>();
        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM users_auth ORDER BY user_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserRecord u = new UserRecord(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getTimestamp("last_login")
                );
                out.add(u);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }
}
