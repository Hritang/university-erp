//package com.university.erp.admin;
//
//import com.university.erp.domain.UserRecord;
//import org.mindrot.jbcrypt.BCrypt;
//
//import java.sql.*;
//import java.util.List;
//
//
//public class AdminService {
//
//    private final AdminDAO adminDAO;
//
//    public AdminService() {
//        this.adminDAO = new AdminDAO();
//    }
//
//
//    // 1. USER MANAGEMENT
//    public AddUserResult addUser(String username, String role, String plainPassword, String status,
//                                 String rollNo, String program, Integer year, String department) {
//        if (username == null || username.isBlank()) return new AddUserResult(false, null, "Username required");
//        if (role == null || role.isBlank()) return new AddUserResult(false, null, "Role required");
//        if (plainPassword == null || plainPassword.length() < 6) return new AddUserResult(false, null, "Password min 6 chars");
//
//        String r = role.trim().toLowerCase();
//        String st = status == null ? "active" : status.trim();
//        // Hash the password
//        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
//
//        Connection conn = null;
//        try {
//            conn = com.university.erp.data.DBConnection.getConnection();
//            conn.setAutoCommit(false);
//
//            // A. Insert into Auth_DB
//            String sqlUser = "INSERT INTO Auth_DB.users_auth (username, role, password_hash, status) VALUES (?, ?, ?, ?)";
//            int userId = -1;
//            try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
//                ps.setString(1, username);
//                ps.setString(2, r);
//                ps.setString(3, hash);
//                ps.setString(4, st);
//                if (ps.executeUpdate() == 0) {
//                    conn.rollback(); return new AddUserResult(false, null, "Failed to insert user");
//                }
//                try (ResultSet rs = ps.getGeneratedKeys()) {
//                    if (rs.next()) userId = rs.getInt(1);
//                    else { conn.rollback(); return new AddUserResult(false, null, "No ID obtained"); }
//                }
//            }
//
//            // B. Insert into ERP_DB
//            if ("student".equals(r)) {
//                String sqlStudent = "INSERT INTO ERP_DB.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
//                try (PreparedStatement ps = conn.prepareStatement(sqlStudent)) {
//                    ps.setInt(1, userId);
//                    ps.setString(2, rollNo);
//                    ps.setString(3, program);
//                    if (year == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, year);
//                    ps.executeUpdate();
//                }
//            } else if ("instructor".equals(r)) {
//                String sqlInstr = "INSERT INTO ERP_DB.instructors (user_id, department) VALUES (?, ?)";
//                try (PreparedStatement ps = conn.prepareStatement(sqlInstr)) {
//                    ps.setInt(1, userId);
//                    ps.setString(2, department);
//                    ps.executeUpdate();
//                }
//            }
//
//            conn.commit();
//            return new AddUserResult(true, userId, "User created successfully");
//
//        } catch (SQLException ex) {
//            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
//            return new AddUserResult(false, null, "DB Error: " + ex.getMessage());
//        } finally {
//            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) {}
//        }
//    }
//
//    // 2. COURSES
//    public boolean createCourse(String code, String title, int credits) {
//        try {
//            return adminDAO.createCourse(code, title, credits) > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean updateCourse(int courseId, String code, String title, int credits) {
//        try {
//            return adminDAO.updateCourse(courseId, code, title, credits);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public List<Course> listCourses() {
//        try { return adminDAO.listCourses(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//
//    // 3. SECTIONS
//    public boolean addSection(int courseId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) {
//        try {
//            // DAO returns new ID (int). We convert to boolean.
//            return adminDAO.createSection(courseId, instructorId, dayTime, room, capacity, semester, year) > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean updateSection(int sectionId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) {
//        try {
//            return adminDAO.updateSection(sectionId, instructorId, dayTime, room, capacity, semester, year);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public List<Section> listSections() {
//        try { return adminDAO.listSections(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public boolean assignInstructorToSection(int sectionId, int instructorId) {
//        try {
//            return adminDAO.assignInstructorToSection(sectionId, instructorId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // 4. USERS & DETAILS READERS
//    public List<UserRecord> listInstructors() {
//        try { return adminDAO.listInstructors(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public List<UserRecord> listAllUsers() {
//        try { return adminDAO.listAllUsers(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public List<AdminDAO.StudentDetail> listStudentDetails() {
//        try { return adminDAO.listStudentDetails(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public List<AdminDAO.InstructorDetail> listInstructorDetails() {
//        try { return adminDAO.listInstructorDetails(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    // 5. SETTINGS
//    public boolean setMaintenance(boolean on, String banner) {
//        try {
//            boolean a = adminDAO.setSetting("maintenance_on", String.valueOf(on));
//            boolean b = adminDAO.setSetting("maintenance_banner", banner);
//            return a || b;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean isMaintenanceOn() {
//        try {
//            return "true".equalsIgnoreCase(adminDAO.getSetting("maintenance_on").orElse("false"));
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean safeDeleteCourse(int courseId) {
//        try {
//            return adminDAO.safeDeleteCourse(courseId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//}
//
//package com.university.erp.admin;
//
//import com.university.erp.domain.UserRecord;
//import org.mindrot.jbcrypt.BCrypt;
//
//import java.sql.*;
//import java.util.List;
//
//public class AdminService {
//
//    private final AdminDAO adminDAO;
//
//    public AdminService() {
//        this.adminDAO = new AdminDAO();
//    }
//
//    // 1. USER MANAGEMENT
//    public AddUserResult addUser(String username, String role, String plainPassword, String status,
//                                 String rollNo, String program, Integer year, String department) {
//        if (username == null || username.isBlank()) return new AddUserResult(false, null, "Username required");
//        if (role == null || role.isBlank()) return new AddUserResult(false, null, "Role required");
//        if (plainPassword == null || plainPassword.length() < 6) return new AddUserResult(false, null, "Password min 6 chars");
//
//        String r = role.trim().toLowerCase();
//        String st = status == null ? "active" : status.trim();
//        // Hash the password
//        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
//
//        Connection conn = null;
//        try {
//            conn = com.university.erp.data.DBConnection.getConnection();
//            conn.setAutoCommit(false);
//
//            // A. Insert into Auth_DB
//            String sqlUser = "INSERT INTO Auth_DB.users_auth (username, role, password_hash, status) VALUES (?, ?, ?, ?)";
//            int userId = -1;
//            try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
//                ps.setString(1, username);
//                ps.setString(2, r);
//                ps.setString(3, hash);
//                ps.setString(4, st);
//                if (ps.executeUpdate() == 0) {
//                    conn.rollback(); return new AddUserResult(false, null, "Failed to insert user");
//                }
//                try (ResultSet rs = ps.getGeneratedKeys()) {
//                    if (rs.next()) userId = rs.getInt(1);
//                    else { conn.rollback(); return new AddUserResult(false, null, "No ID obtained"); }
//                }
//            }
//
//            // B. Insert into ERP_DB
//            if ("student".equals(r)) {
//                String sqlStudent = "INSERT INTO ERP_DB.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
//                try (PreparedStatement ps = conn.prepareStatement(sqlStudent)) {
//                    ps.setInt(1, userId);
//                    ps.setString(2, rollNo);
//                    ps.setString(3, program);
//                    if (year == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, year);
//                    ps.executeUpdate();
//                }
//            } else if ("instructor".equals(r)) {
//                String sqlInstr = "INSERT INTO ERP_DB.instructors (user_id, department) VALUES (?, ?)";
//                try (PreparedStatement ps = conn.prepareStatement(sqlInstr)) {
//                    ps.setInt(1, userId);
//                    ps.setString(2, department);
//                    ps.executeUpdate();
//                }
//            }
//
//            conn.commit();
//            return new AddUserResult(true, userId, "User created successfully");
//
//        } catch (SQLException ex) {
//            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
//            return new AddUserResult(false, null, "DB Error: " + ex.getMessage());
//        } finally {
//            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) {}
//        }
//    }
//
//    // 2. COURSES
//    public boolean createCourse(String code, String title, int credits) {
//        try {
//            return adminDAO.createCourse(code, title, credits) > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean updateCourse(int courseId, String code, String title, int credits) {
//        try {
//            return adminDAO.updateCourse(courseId, code, title, credits);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public List<Course> listCourses() {
//        try { return adminDAO.listCourses(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    /**
//     * Wrapper for safeDeleteCourse in DAO.
//     * Returns false if course has enrolled students and cannot be deleted.
//     */
//    public boolean safeDeleteCourse(int courseId) {
//        try {
//            return adminDAO.safeDeleteCourse(courseId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // 3. SECTIONS
//    public boolean addSection(int courseId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) {
//        try {
//            // DAO returns new ID (int). We convert to boolean.
//            return adminDAO.createSection(courseId, instructorId, dayTime, room, capacity, semester, year) > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean updateSection(int sectionId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) {
//        try {
//            return adminDAO.updateSection(sectionId, instructorId, dayTime, room, capacity, semester, year);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public List<Section> listSections() {
//        try { return adminDAO.listSections(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public boolean assignInstructorToSection(int sectionId, int instructorId) {
//        try {
//            return adminDAO.assignInstructorToSection(sectionId, instructorId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * Wrapper for safeDeleteSection:
//     * - false means: section has enrolled students → cannot delete
//     */
//    public boolean safeDeleteSection(int sectionId) {
//        try {
//            return adminDAO.safeDeleteSection(sectionId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // 4. USERS & DETAILS READERS
//    public List<UserRecord> listInstructors() {
//        try { return adminDAO.listInstructors(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public List<UserRecord> listAllUsers() {
//        try { return adminDAO.listAllUsers(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public List<AdminDAO.StudentDetail> listStudentDetails() {
//        try { return adminDAO.listStudentDetails(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    public List<AdminDAO.InstructorDetail> listInstructorDetails() {
//        try { return adminDAO.listInstructorDetails(); } catch (SQLException e) { throw new RuntimeException(e); }
//    }
//
//    /**
//     * Wrapper for safeDeleteUser:
//     * - false => user has enrollments/sections or doesn't exist
//     *
//     * NOTE: in your UI, BEFORE calling this:
//     *   - check that userId != currentLoggedInAdminId
//     */
//    public boolean safeDeleteUser(int userId) {
//        try {
//            return adminDAO.safeDeleteUser(userId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // 5. SETTINGS
//    public boolean setMaintenance(boolean on, String banner) {
//        try {
//            boolean a = adminDAO.setSetting("maintenance_on", String.valueOf(on));
//            boolean b = adminDAO.setSetting("maintenance_banner", banner);
//            return a || b;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean isMaintenanceOn() {
//        try {
//            return "true".equalsIgnoreCase(adminDAO.getSetting("maintenance_on").orElse("false"));
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//}

package com.university.erp.admin;

import com.university.erp.domain.UserRecord;
import com.university.erp.data.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final AdminDAO adminDAO;
    private int loggedInAdminId = -1;

    public AdminService() {
        this.adminDAO = new AdminDAO();
    }

    // --- 0. ADMIN SESSION HELPERS ---
    public void setLoggedInAdminId(int id) { this.loggedInAdminId = id; }
    public int getLoggedInAdminId() { return loggedInAdminId; }

    // -------------------------------------------------------------------------
    // 1. USER MANAGEMENT
    // -------------------------------------------------------------------------

    // FIX: Removed "AdminDAO." prefix. Uses your standalone AddUserResult class.
    public AddUserResult addUser(String username, String role, String plainPassword, String status,
                                 String rollNo, String program, Integer year, String department) {

        if (username == null || username.isBlank()) return new AddUserResult(false, null, "Username required");
        if (role == null || role.isBlank()) return new AddUserResult(false, null, "Role required");
        if (plainPassword == null || plainPassword.length() < 6) return new AddUserResult(false, null, "Password min 6 chars");

        String r = role.trim().toLowerCase();
        String st = status == null ? "active" : status.trim();
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // A. Insert into Auth_DB
            String sqlUser = "INSERT INTO Auth_DB.users_auth (username, role, password_hash, status) VALUES (?, ?, ?, ?)";
            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, r);
                ps.setString(3, hash);
                ps.setString(4, st);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return new AddUserResult(false, null, "Failed to insert user");
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) userId = rs.getInt(1);
                    else {
                        conn.rollback();
                        return new AddUserResult(false, null, "No ID obtained");
                    }
                }
            }

            // B. Insert into ERP_DB
            if ("student".equals(r)) {
                String sqlStudent = "INSERT INTO ERP_DB.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlStudent)) {
                    ps.setInt(1, userId);
                    ps.setString(2, rollNo);
                    ps.setString(3, program);
                    if (year == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, year);
                    ps.executeUpdate();
                }
            } else if ("instructor".equals(r)) {
                String sqlInstr = "INSERT INTO ERP_DB.instructors (user_id, department) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInstr)) {
                    ps.setInt(1, userId);
                    ps.setString(2, department);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return new AddUserResult(true, userId, "User created successfully");

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
            return new AddUserResult(false, null, "DB Error: " + ex.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) {}
        }
    }

    // --- DELETE USER ---
    // --- DELETE USER (Fixed for Students with Grades) ---
    public boolean deleteUser(int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. If user is an Instructor, unassign them from any SECTIONS first
            try (PreparedStatement ps = conn.prepareStatement("UPDATE ERP_DB.sections SET instructor_id = NULL WHERE instructor_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 2. If user is a Student, we must delete their GRADES first (Fix for constraint error)
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM ERP_DB.grades WHERE enrollment_id IN (SELECT enrollment_id FROM ERP_DB.enrollments WHERE student_id = ?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 3. Now we can safely delete the Enrollments
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.enrollments WHERE student_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 4. Delete from Role Tables
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.students WHERE user_id=?")) {
                ps.setInt(1, userId); ps.executeUpdate();
            }
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.instructors WHERE user_id=?")) {
                ps.setInt(1, userId); ps.executeUpdate();
            }

            // 5. Finally, Delete from Auth table
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM Auth_DB.users_auth WHERE user_id=?")) {
                ps.setInt(1, userId); ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch(Exception e) {
            if(conn != null) try { conn.rollback(); } catch(Exception ex){}
            e.printStackTrace();
            return false;
        } finally {
            if(conn != null) try { conn.setAutoCommit(true); conn.close(); } catch(Exception ex){}
        }
    }

    // -------------------------------------------------------------------------
    // 2. COURSES
    // -------------------------------------------------------------------------
    public boolean createCourse(String code, String title, int credits) {
        try { return adminDAO.createCourse(code, title, credits) > 0; } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateCourse(int courseId, String code, String title, int credits) {
        try { return adminDAO.updateCourse(courseId, code, title, credits); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Course> listCourses() {
        try { return adminDAO.listCourses(); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public boolean canDeleteCourse(int courseId) {
        String sql = "SELECT 1 FROM ERP_DB.enrollments e JOIN ERP_DB.sections s ON e.section_id = s.section_id WHERE s.course_id = ? LIMIT 1";
        try(Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try(ResultSet rs = ps.executeQuery()) { return !rs.next(); }
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public List<Section> getSectionsByCourse(int courseId) {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT section_id FROM ERP_DB.sections WHERE course_id = ?";
        try(Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    // Using placeholder values to satisfy constructor
                    Section s = new Section(rs.getInt(1), 0, "", "", null, "", "", "", 0, "", 0);
                    list.add(s);
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteCourse(int courseId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.sections WHERE course_id=?")) {
                ps.setInt(1, courseId); ps.executeUpdate();
            }
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.courses WHERE course_id=?")) {
                ps.setInt(1, courseId); ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch(Exception e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            e.printStackTrace();
            return false;
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true); conn.close();}catch(Exception ex){}
        }
    }

    // -------------------------------------------------------------------------
    // 3. SECTIONS
    // -------------------------------------------------------------------------
    public boolean addSection(int courseId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) {
        try { return adminDAO.createSection(courseId, instructorId, dayTime, room, capacity, semester, year) > 0; } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateSection(int sectionId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) {
        try { return adminDAO.updateSection(sectionId, instructorId, dayTime, room, capacity, semester, year); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Section> listSections() {
        try { return adminDAO.listSections(); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public boolean assignInstructorToSection(int sectionId, int instructorId) {
        try { return adminDAO.assignInstructorToSection(sectionId, instructorId); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean canDeleteSection(int sectionId) {
        String sql = "SELECT 1 FROM ERP_DB.enrollments WHERE section_id = ? LIMIT 1";
        try(Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try(ResultSet rs = ps.executeQuery()) { return !rs.next(); }
        } catch(Exception e) { return false; }
    }

    public boolean deleteSection(int sectionId) {
        try(Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM ERP_DB.sections WHERE section_id=?")) {
            ps.setInt(1, sectionId);
            return ps.executeUpdate() > 0;
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public boolean deleteSectionForced(int sectionId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.enrollments WHERE section_id=?")) {
                ps.setInt(1, sectionId); ps.executeUpdate();
            }
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM ERP_DB.sections WHERE section_id=?")) {
                ps.setInt(1, sectionId); ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch(Exception e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            return false;
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true); conn.close();}catch(Exception ex){}
        }
    }

    // -------------------------------------------------------------------------
    // 4. READERS
    // -------------------------------------------------------------------------
    public List<UserRecord> listInstructors() {
        try { return adminDAO.listInstructors(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
    public List<UserRecord> listAllUsers() {
        try { return adminDAO.listAllUsers(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
    public List<AdminDAO.StudentDetail> listStudentDetails() {
        try { return adminDAO.listStudentDetails(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
    public List<AdminDAO.InstructorDetail> listInstructorDetails() {
        try { return adminDAO.listInstructorDetails(); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // -------------------------------------------------------------------------
    // 5. SETTINGS
    // -------------------------------------------------------------------------
    public boolean setMaintenance(boolean on, String banner) {
        try {
            boolean a = adminDAO.setSetting("maintenance_on", String.valueOf(on));
            boolean b = adminDAO.setSetting("maintenance_banner", banner);
            return a || b;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean isMaintenanceOn() {
        try {
            return "true".equalsIgnoreCase(adminDAO.getSetting("maintenance_on").orElse("false"));
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}