//package com.university.erp.admin;
//
//import com.university.erp.data.DBConnection;
//import com.university.erp.domain.UserRecord;
//
//import java.sql.*;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//public class AdminDAO {
//
//    // USERS
//    public int createUserAuto(String username, String role, String passwordHash, String status) throws SQLException {
//        String sql = "INSERT INTO Auth_DB.users_auth (username, role, password_hash, status) VALUES (?, ?, ?, ?)";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setString(1, username);
//            ps.setString(2, role);
//            ps.setString(3, passwordHash);
//            ps.setString(4, status);
//            int affected = ps.executeUpdate();
//            if (affected == 0) return -1;
//            try (ResultSet rs = ps.getGeneratedKeys()) {
//                if (rs.next()) return rs.getInt(1);
//            }
//        }
//        return -1;
//    }
//
//    public boolean createStudentRow(int userId, String rollNo, String program, Integer year) throws SQLException {
//        String sql = "INSERT INTO ERP_DB.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ps.setString(2, rollNo);
//            ps.setString(3, program);
//            if (year == null) ps.setNull(4, Types.INTEGER);
//            else ps.setInt(4, year);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public boolean createInstructorRow(int userId, String department) throws SQLException {
//        String sql = "INSERT INTO ERP_DB.instructors (user_id, department) VALUES (?, ?)";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ps.setString(2, department);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public boolean updateUserStatus(int userId, String newStatus) throws SQLException {
//        String sql = "UPDATE Auth_DB.users_auth SET status = ? WHERE user_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, newStatus);
//            ps.setInt(2, userId);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public Optional<UserRecord> findUserById(int userId) throws SQLException {
//        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM Auth_DB.users_auth WHERE user_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    return Optional.of(new UserRecord(
//                            rs.getInt("user_id"),
//                            rs.getString("username"),
//                            rs.getString("role"),
//                            rs.getString("password_hash"),
//                            rs.getString("status"),
//                            rs.getTimestamp("last_login")
//                    ));
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    // Student/Instructor detail lists
//    public static class StudentDetail {
//        private final int userId;
//        private final String username;
//        private final String status;
//        private final String rollNo;
//        private final String program;
//        private final Integer year;
//
//        public StudentDetail(int userId, String username, String status, String rollNo, String program, Integer year) {
//            this.userId = userId;
//            this.username = username;
//            this.status = status;
//            this.rollNo = rollNo;
//            this.program = program;
//            this.year = year;
//        }
//
//        public int getUserId() { return userId; }
//        public String getUsername() { return username; }
//        public String getStatus() { return status; }
//        public String getRollNo() { return rollNo; }
//        public String getProgram() { return program; }
//        public Integer getYear() { return year; }
//    }
//
//    public static class InstructorDetail {
//        private final int userId;
//        private final String username;
//        private final String status;
//        private final String department;
//
//        public InstructorDetail(int userId, String username, String status, String department) {
//            this.userId = userId;
//            this.username = username;
//            this.status = status;
//            this.department = department;
//        }
//
//        public int getUserId() { return userId; }
//        public String getUsername() { return username; }
//        public String getStatus() { return status; }
//        public String getDepartment() { return department; }
//    }
//
//    //to list all users (basic UserRecord)
//    public List<UserRecord> listAllUsers() throws SQLException {
//        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM Auth_DB.users_auth ORDER BY username";
//        List<UserRecord> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                out.add(new UserRecord(
//                        rs.getInt("user_id"),
//                        rs.getString("username"),
//                        rs.getString("role"),
//                        rs.getString("password_hash"),
//                        rs.getString("status"),
//                        rs.getTimestamp("last_login")
//                ));
//            }
//        }
//        return out;
//    }
//
//    // detailed student list (joins students)
//    public List<StudentDetail> listStudentDetails() throws SQLException {
//        String sql = """
//            SELECT au.user_id, au.username, au.status, s.roll_no, s.program, s.year
//            FROM Auth_DB.users_auth au
//            JOIN ERP_DB.students s ON au.user_id = s.user_id
//            WHERE au.role = 'student'
//            ORDER BY au.username
//            """;
//        List<StudentDetail> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                Integer year = rs.getObject("year") == null ? null : rs.getInt("year");
//                out.add(new StudentDetail(
//                        rs.getInt("user_id"),
//                        rs.getString("username"),
//                        rs.getString("status"),
//                        rs.getString("roll_no"),
//                        rs.getString("program"),
//                        year
//                ));
//            }
//        }
//        return out;
//    }
//
//    // detailed instructor list
//    public List<InstructorDetail> listInstructorDetails() throws SQLException {
//        String sql = """
//            SELECT au.user_id, au.username, au.status, i.department
//            FROM Auth_DB.users_auth au
//            JOIN ERP_DB.instructors i ON au.user_id = i.user_id
//            WHERE au.role = 'instructor'
//            ORDER BY au.username
//            """;
//        List<InstructorDetail> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                out.add(new InstructorDetail(
//                        rs.getInt("user_id"),
//                        rs.getString("username"),
//                        rs.getString("status"),
//                        rs.getString("department")
//                ));
//            }
//        }
//        return out;
//    }
//
//
//    // COURSES
//    public int createCourse(String code, String title, int credits) throws SQLException {
//        String sql = "INSERT INTO ERP_DB.courses (code, title, credits) VALUES (?, ?, ?)";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setString(1, code);
//            ps.setString(2, title);
//            ps.setInt(3, credits);
//            int affected = ps.executeUpdate();
//            if (affected == 0) return -1;
//            try (ResultSet rs = ps.getGeneratedKeys()) {
//                if (rs.next()) return rs.getInt(1);
//            }
//        }
//        return -1;
//    }
//
//    public boolean updateCourse(int courseId, String code, String title, int credits) throws SQLException {
//        String sql = "UPDATE ERP_DB.courses SET code = ?, title = ?, credits = ? WHERE course_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, code); ps.setString(2, title); ps.setInt(3, credits); ps.setInt(4, courseId);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public List<Course> listCourses() throws SQLException {
//        String sql = "SELECT course_id, code, title, credits FROM ERP_DB.courses ORDER BY code";
//        List<Course> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                out.add(new Course(
//                        rs.getInt("course_id"),
//                        rs.getString("code"),
//                        rs.getString("title"),
//                        rs.getInt("credits")
//                ));
//            }
//        }
//        return out;
//    }
//
//    public Optional<Course> findCourseById(int courseId) throws SQLException {
//        String sql = "SELECT course_id, code, title, credits FROM ERP_DB.courses WHERE course_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, courseId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    return Optional.of(new Course(
//                            rs.getInt("course_id"),
//                            rs.getString("code"),
//                            rs.getString("title"),
//                            rs.getInt("credits")
//                    ));
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    // SECTIONS
//    public int createSection(int courseId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
//        String sql = "INSERT INTO ERP_DB.sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setInt(1, courseId);
//            if (instructorId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, instructorId);
//            ps.setString(3, dayTime); ps.setString(4, room); ps.setInt(5, capacity); ps.setString(6, semester); ps.setInt(7, year);
//            int affected = ps.executeUpdate();
//            if (affected == 0) return -1;
//            try (ResultSet rs = ps.getGeneratedKeys()) {
//                if (rs.next()) return rs.getInt(1);
//            }
//        }
//        return -1;
//    }
//
//    public boolean updateSection(int sectionId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
//        String sql = "UPDATE ERP_DB.sections SET instructor_id = ?, day_time = ?, room = ?, capacity = ?, semester = ?, year = ? WHERE section_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            if (instructorId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, instructorId);
//            ps.setString(2, dayTime); ps.setString(3, room); ps.setInt(4, capacity); ps.setString(5, semester); ps.setInt(6, year);
//            ps.setInt(7, sectionId);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public boolean assignInstructorToSection(int sectionId, int instructorId) throws SQLException {
//        String sql = "UPDATE ERP_DB.sections SET instructor_id = ? WHERE section_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, instructorId); ps.setInt(2, sectionId);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public Optional<Section> findSectionById(int sectionId) throws SQLException {
//        String sql = """
//                SELECT s.section_id, s.course_id, c.code AS course_code, c.title AS course_title,
//                       s.instructor_id, au.username AS instructor_username,
//                       s.day_time, s.room, s.capacity, s.semester, s.year
//                FROM ERP_DB.sections s
//                LEFT JOIN ERP_DB.courses c ON s.course_id = c.course_id
//                LEFT JOIN Auth_DB.users_auth au ON s.instructor_id = au.user_id
//                WHERE s.section_id = ?
//                """;
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, sectionId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    Section s = new Section(
//                            rs.getInt("section_id"),
//                            rs.getInt("course_id"),
//                            rs.getString("course_code"),
//                            rs.getString("course_title"),
//                            rs.getObject("instructor_id") == null ? null : rs.getInt("instructor_id"),
//                            rs.getString("instructor_username"),
//                            rs.getString("day_time"),
//                            rs.getString("room"),
//                            rs.getInt("capacity"),
//                            rs.getString("semester"),
//                            rs.getInt("year")
//                    );
//                    return Optional.of(s);
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    public List<Section> listSections() throws SQLException {
//        String sql = """
//                SELECT s.section_id, s.course_id, c.code AS course_code, c.title AS course_title,
//                       s.instructor_id, au.username AS instructor_username,
//                       s.day_time, s.room, s.capacity, s.semester, s.year
//                FROM ERP_DB.sections s
//                LEFT JOIN ERP_DB.courses c ON s.course_id = c.course_id
//                LEFT JOIN Auth_DB.users_auth au ON s.instructor_id = au.user_id
//                ORDER BY c.code, s.section_id
//                """;
//        List<Section> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                Section s = new Section(
//                        rs.getInt("section_id"),
//                        rs.getInt("course_id"),
//                        rs.getString("course_code"),
//                        rs.getString("course_title"),
//                        rs.getObject("instructor_id") == null ? null : rs.getInt("instructor_id"),
//                        rs.getString("instructor_username"),
//                        rs.getString("day_time"),
//                        rs.getString("room"),
//                        rs.getInt("capacity"),
//                        rs.getString("semester"),
//                        rs.getInt("year")
//                );
//                out.add(s);
//            }
//        }
//        return out;
//    }
//
//
//    // Student/Instructor single-row find & updates
//    public Optional<Student> findStudentByUserId(int userId) throws SQLException {
//        String sql = "SELECT user_id, roll_no, program, year FROM ERP_DB.students WHERE user_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    Student s = new Student(
//                            rs.getInt("user_id"),
//                            rs.getString("roll_no"),
//                            rs.getString("program"),
//                            rs.getObject("year") == null ? null : rs.getInt("year")
//                    );
//                    return Optional.of(s);
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    public boolean updateStudentRow(int userId, String rollNo, String program, Integer year) throws SQLException {
//        String sql = "UPDATE ERP_DB.students SET roll_no = ?, program = ?, year = ? WHERE user_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, rollNo);
//            ps.setString(2, program);
//            if (year == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, year);
//            ps.setInt(4, userId);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public Optional<Instructor> findInstructorByUserId(int userId) throws SQLException {
//        String sql = "SELECT user_id, department FROM ERP_DB.instructors WHERE user_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    Instructor i = new Instructor(rs.getInt("user_id"), rs.getString("department"));
//                    return Optional.of(i);
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    public boolean updateInstructorRow(int userId, String department) throws SQLException {
//        String sql = "UPDATE ERP_DB.instructors SET department = ? WHERE user_id = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, department);
//            ps.setInt(2, userId);
//            return ps.executeUpdate() == 1;
//        }
//    }
//
//    public static class Student {
//        private final int userId;
//        private final String rollNo;
//        private final String program;
//        private final Integer year;
//
//        public Student(int userId, String rollNo, String program, Integer year) {
//            this.userId = userId;
//            this.rollNo = rollNo;
//            this.program = program;
//            this.year = year;
//        }
//        public int getUserId() { return userId; }
//        public String getRollNo() { return rollNo; }
//        public String getProgram() { return program; }
//        public Integer getYear() { return year; }
//    }
//
//    public static class Instructor {
//        private final int userId;
//        private final String department;
//        public Instructor(int userId, String department) {
//            this.userId = userId; this.department = department;
//        }
//        public int getUserId() { return userId; }
//        public String getDepartment() { return department; }
//    }
//
//    // ROLE LISTS (Auth_DB joins)
//    public List<UserRecord> listInstructors() throws SQLException {
//        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM Auth_DB.users_auth WHERE role = 'instructor' ORDER BY username";
//        List<UserRecord> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                out.add(new UserRecord(
//                        rs.getInt("user_id"),
//                        rs.getString("username"),
//                        rs.getString("role"),
//                        rs.getString("password_hash"),
//                        rs.getString("status"),
//                        rs.getTimestamp("last_login")
//                ));
//            }
//        }
//        return out;
//    }
//
//    public List<UserRecord> listStudents() throws SQLException {
//        String sql = "SELECT au.user_id, au.username, au.role, au.password_hash, au.status, au.last_login " +
//                "FROM Auth_DB.users_auth au " +
//                "JOIN ERP_DB.students s ON au.user_id = s.user_id " +
//                "WHERE au.role = 'student' ORDER BY au.username";
//        List<UserRecord> out = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                out.add(new UserRecord(
//                        rs.getInt("user_id"),
//                        rs.getString("username"),
//                        rs.getString("role"),
//                        rs.getString("password_hash"),
//                        rs.getString("status"),
//                        rs.getTimestamp("last_login")
//                ));
//            }
//        }
//        return out;
//    }
//
//
//    // SETTINGS
//    public boolean setSetting(String key, String value) throws SQLException {
//        String sql = "REPLACE INTO ERP_DB.settings (key_, value) VALUES (?, ?)";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, key);
//            ps.setString(2, value);
//            return ps.executeUpdate() >= 1;
//        }
//    }
//
//    public Optional<String> getSetting(String key) throws SQLException {
//        String sql = "SELECT value FROM ERP_DB.settings WHERE key_ = ?";
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//            ps.setString(1, key);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) return Optional.ofNullable(rs.getString("value"));
//            }
//        }
//        return Optional.empty();
//    }
//
//
//    // BACKUP / RESTORE
//    public String backupERP(String tag) throws SQLException {
//        if (tag == null || tag.isBlank()) {
//            tag = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
//        }
//        String[] tables = new String[] {
//                "courses", "sections", "students", "instructors", "enrollments", "grades", "settings"
//        };
//
//        Connection c = null;
//        Statement st = null;
//        try {
//            c = DBConnection.getConnection();
//            c.setAutoCommit(false);
//            st = c.createStatement();
//            for (String t : tables) {
//                String bt = t + "_backup_" + tag;
//                st.executeUpdate("DROP TABLE IF EXISTS ERP_DB." + bt);
//                st.executeUpdate("CREATE TABLE ERP_DB." + bt + " AS SELECT * FROM ERP_DB." + t);
//            }
//            c.commit();
//            return tag;
//        } catch (SQLException ex) {
//            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
//            throw ex;
//        } finally {
//            if (st != null) try { st.close(); } catch (SQLException ignore) {}
//            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignore) {}
//        }
//    }
//
//    public boolean restoreERP(String tag) throws SQLException {
//        if (tag == null || tag.isBlank()) throw new SQLException("backup tag required");
//
//        String[] tablesInOrder = new String[] {
//                "settings", "grades", "enrollments", "sections", "courses", "instructors", "students"
//        };
//
//        Connection c = null;
//        Statement st = null;
//        try {
//            c = DBConnection.getConnection();
//            c.setAutoCommit(false);
//            st = c.createStatement();
//
//            for (String t : tablesInOrder) {
//                String bt = "ERP_DB." + t + "_backup_" + tag;
//                String checkSql = "SELECT 1 FROM " + bt + " LIMIT 1";
//                try (ResultSet rs = st.executeQuery(checkSql)) {}
//            }
//            st.execute("SET FOREIGN_KEY_CHECKS = 0");
//            for (String t : tablesInOrder) {
//                String bt = t + "_backup_" + tag;
//                st.executeUpdate("TRUNCATE TABLE ERP_DB." + t);
//                st.executeUpdate("INSERT INTO ERP_DB." + t + " SELECT * FROM ERP_DB." + bt);
//            }
//            st.execute("SET FOREIGN_KEY_CHECKS = 1");
//            c.commit();
//            return true;
//        } catch (SQLException ex) {
//            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
//            throw ex;
//        } finally {
//            if (st != null) try { st.close(); } catch (SQLException ignore) {}
//            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignore) {}
//        }
//    }
//
//    public boolean safeDeleteCourse(int courseId) throws SQLException {
//        try (Connection c = DBConnection.getConnection()) {
//            c.setAutoCommit(false);
//
//            // Check if any section has students
//            String check = """
//            SELECT COUNT(*) AS cnt
//            FROM ERP_DB.enrollments en
//            JOIN ERP_DB.sections s ON en.section_id = s.section_id
//            WHERE s.course_id = ?
//        """;
//            try (PreparedStatement ps = c.prepareStatement(check)) {
//                ps.setInt(1, courseId);
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next() && rs.getInt("cnt") > 0) {
//                        c.rollback();
//                        return false; // cannot delete
//                    }
//                }
//            }
//
//            // Delete empty sections
//            String delSections = "DELETE FROM ERP_DB.sections WHERE course_id = ?";
//            try (PreparedStatement ps = c.prepareStatement(delSections)) {
//                ps.setInt(1, courseId);
//                ps.executeUpdate();
//            }
//
//            // Delete course
//            String delCourse = "DELETE FROM ERP_DB.courses WHERE course_id = ?";
//            try (PreparedStatement ps = c.prepareStatement(delCourse)) {
//                ps.setInt(1, courseId);
//                ps.executeUpdate();
//            }
//
//            c.commit();
//            return true;
//        }
//    }
//
//
//
//}

package com.university.erp.admin;

import com.university.erp.data.DBConnection;
import com.university.erp.domain.UserRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDAO {

    // USERS
    public int createUserAuto(String username, String role, String passwordHash, String status) throws SQLException {
        String sql = "INSERT INTO Auth_DB.users_auth (username, role, password_hash, status) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.setString(3, passwordHash);
            ps.setString(4, status);
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public boolean createStudentRow(int userId, String rollNo, String program, Integer year) throws SQLException {
        String sql = "INSERT INTO ERP_DB.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, rollNo);
            ps.setString(3, program);
            if (year == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, year);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean createInstructorRow(int userId, String department) throws SQLException {
        String sql = "INSERT INTO ERP_DB.instructors (user_id, department) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, department);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateUserStatus(int userId, String newStatus) throws SQLException {
        String sql = "UPDATE Auth_DB.users_auth SET status = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public Optional<UserRecord> findUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM Auth_DB.users_auth WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UserRecord(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("password_hash"),
                            rs.getString("status"),
                            rs.getTimestamp("last_login")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    // Student/Instructor detail lists
    public static class StudentDetail {
        private final int userId;
        private final String username;
        private final String status;
        private final String rollNo;
        private final String program;
        private final Integer year;

        public StudentDetail(int userId, String username, String status, String rollNo, String program, Integer year) {
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

    public static class InstructorDetail {
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

    //to list all users (basic UserRecord)
    public List<UserRecord> listAllUsers() throws SQLException {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM Auth_DB.users_auth ORDER BY username";
        List<UserRecord> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new UserRecord(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getTimestamp("last_login")
                ));
            }
        }
        return out;
    }

    // detailed student list (joins students)
    public List<StudentDetail> listStudentDetails() throws SQLException {
        String sql = """
            SELECT au.user_id, au.username, au.status, s.roll_no, s.program, s.year
            FROM Auth_DB.users_auth au
            JOIN ERP_DB.students s ON au.user_id = s.user_id
            WHERE au.role = 'student'
            ORDER BY au.username
            """;
        List<StudentDetail> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer year = rs.getObject("year") == null ? null : rs.getInt("year");
                out.add(new StudentDetail(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("status"),
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        year
                ));
            }
        }
        return out;
    }

    // detailed instructor list
    public List<InstructorDetail> listInstructorDetails() throws SQLException {
        String sql = """
            SELECT au.user_id, au.username, au.status, i.department
            FROM Auth_DB.users_auth au
            JOIN ERP_DB.instructors i ON au.user_id = i.user_id
            WHERE au.role = 'instructor'
            ORDER BY au.username
            """;
        List<InstructorDetail> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new InstructorDetail(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("status"),
                        rs.getString("department")
                ));
            }
        }
        return out;
    }


    // COURSES
    public int createCourse(String code, String title, int credits) throws SQLException {
        String sql = "INSERT INTO ERP_DB.courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateCourse(int courseId, String code, String title, int credits) throws SQLException {
        String sql = "UPDATE ERP_DB.courses SET code = ?, title = ?, credits = ? WHERE course_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, title); ps.setInt(3, credits); ps.setInt(4, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Course> listCourses() throws SQLException {
        String sql = "SELECT course_id, code, title, credits FROM ERP_DB.courses ORDER BY code";
        List<Course> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits")
                ));
            }
        }
        return out;
    }

    public Optional<Course> findCourseById(int courseId) throws SQLException {
        String sql = "SELECT course_id, code, title, credits FROM ERP_DB.courses WHERE course_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Course(
                            rs.getInt("course_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("credits")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    // SECTIONS
    public int createSection(int courseId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "INSERT INTO ERP_DB.sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            if (instructorId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, instructorId);
            ps.setString(3, dayTime); ps.setString(4, room); ps.setInt(5, capacity); ps.setString(6, semester); ps.setInt(7, year);
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateSection(int sectionId, Integer instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "UPDATE ERP_DB.sections SET instructor_id = ?, day_time = ?, room = ?, capacity = ?, semester = ?, year = ? WHERE section_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (instructorId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, instructorId);
            ps.setString(2, dayTime); ps.setString(3, room); ps.setInt(4, capacity); ps.setString(5, semester); ps.setInt(6, year);
            ps.setInt(7, sectionId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean assignInstructorToSection(int sectionId, int instructorId) throws SQLException {
        String sql = "UPDATE ERP_DB.sections SET instructor_id = ? WHERE section_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instructorId); ps.setInt(2, sectionId);
            return ps.executeUpdate() == 1;
        }
    }

    public Optional<Section> findSectionById(int sectionId) throws SQLException {
        String sql = """
                SELECT s.section_id, s.course_id, c.code AS course_code, c.title AS course_title,
                       s.instructor_id, au.username AS instructor_username,
                       s.day_time, s.room, s.capacity, s.semester, s.year
                FROM ERP_DB.sections s
                LEFT JOIN ERP_DB.courses c ON s.course_id = c.course_id
                LEFT JOIN Auth_DB.users_auth au ON s.instructor_id = au.user_id
                WHERE s.section_id = ?
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Section s = new Section(
                            rs.getInt("section_id"),
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getObject("instructor_id") == null ? null : rs.getInt("instructor_id"),
                            rs.getString("instructor_username"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity"),
                            rs.getString("semester"),
                            rs.getInt("year")
                    );
                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    public List<Section> listSections() throws SQLException {
        String sql = """
                SELECT s.section_id, s.course_id, c.code AS course_code, c.title AS course_title,
                       s.instructor_id, au.username AS instructor_username,
                       s.day_time, s.room, s.capacity, s.semester, s.year
                FROM ERP_DB.sections s
                LEFT JOIN ERP_DB.courses c ON s.course_id = c.course_id
                LEFT JOIN Auth_DB.users_auth au ON s.instructor_id = au.user_id
                ORDER BY c.code, s.section_id
                """;
        List<Section> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Section s = new Section(
                        rs.getInt("section_id"),
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getObject("instructor_id") == null ? null : rs.getInt("instructor_id"),
                        rs.getString("instructor_username"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                );
                out.add(s);
            }
        }
        return out;
    }


    // Student/Instructor single-row find & updates
    public Optional<Student> findStudentByUserId(int userId) throws SQLException {
        String sql = "SELECT user_id, roll_no, program, year FROM ERP_DB.students WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student(
                            rs.getInt("user_id"),
                            rs.getString("roll_no"),
                            rs.getString("program"),
                            rs.getObject("year") == null ? null : rs.getInt("year")
                    );
                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateStudentRow(int userId, String rollNo, String program, Integer year) throws SQLException {
        String sql = "UPDATE ERP_DB.students SET roll_no = ?, program = ?, year = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setString(2, program);
            if (year == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, year);
            ps.setInt(4, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public Optional<Instructor> findInstructorByUserId(int userId) throws SQLException {
        String sql = "SELECT user_id, department FROM ERP_DB.instructors WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instructor i = new Instructor(rs.getInt("user_id"), rs.getString("department"));
                    return Optional.of(i);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateInstructorRow(int userId, String department) throws SQLException {
        String sql = "UPDATE ERP_DB.instructors SET department = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, department);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public static class Student {
        private final int userId;
        private final String rollNo;
        private final String program;
        private final Integer year;

        public Student(int userId, String rollNo, String program, Integer year) {
            this.userId = userId;
            this.rollNo = rollNo;
            this.program = program;
            this.year = year;
        }
        public int getUserId() { return userId; }
        public String getRollNo() { return rollNo; }
        public String getProgram() { return program; }
        public Integer getYear() { return year; }
    }

    public static class Instructor {
        private final int userId;
        private final String department;
        public Instructor(int userId, String department) {
            this.userId = userId; this.department = department;
        }
        public int getUserId() { return userId; }
        public String getDepartment() { return department; }
    }

    // ROLE LISTS (Auth_DB joins)
    public List<UserRecord> listInstructors() throws SQLException {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login FROM Auth_DB.users_auth WHERE role = 'instructor' ORDER BY username";
        List<UserRecord> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new UserRecord(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getTimestamp("last_login")
                ));
            }
        }
        return out;
    }

    public List<UserRecord> listStudents() throws SQLException {
        String sql = "SELECT au.user_id, au.username, au.role, au.password_hash, au.status, au.last_login " +
                "FROM Auth_DB.users_auth au " +
                "JOIN ERP_DB.students s ON au.user_id = s.user_id " +
                "WHERE au.role = 'student' ORDER BY au.username";
        List<UserRecord> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new UserRecord(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getTimestamp("last_login")
                ));
            }
        }
        return out;
    }


    // SETTINGS
    public boolean setSetting(String key, String value) throws SQLException {
        String sql = "REPLACE INTO ERP_DB.settings (key_, value) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() >= 1;
        }
    }

    public Optional<String> getSetting(String key) throws SQLException {
        String sql = "SELECT value FROM ERP_DB.settings WHERE key_ = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString("value"));
            }
        }
        return Optional.empty();
    }


    // BACKUP / RESTORE
    public String backupERP(String tag) throws SQLException {
        if (tag == null || tag.isBlank()) {
            tag = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        }
        String[] tables = new String[] {
                "courses", "sections", "students", "instructors", "enrollments", "grades", "settings"
        };

        Connection c = null;
        Statement st = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);
            st = c.createStatement();
            for (String t : tables) {
                String bt = t + "_backup_" + tag;
                st.executeUpdate("DROP TABLE IF EXISTS ERP_DB." + bt);
                st.executeUpdate("CREATE TABLE ERP_DB." + bt + " AS SELECT * FROM ERP_DB." + t);
            }
            c.commit();
            return tag;
        } catch (SQLException ex) {
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
            throw ex;
        } finally {
            if (st != null) try { st.close(); } catch (SQLException ignore) {}
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignore) {}
        }
    }

    public boolean restoreERP(String tag) throws SQLException {
        if (tag == null || tag.isBlank()) throw new SQLException("backup tag required");

        String[] tablesInOrder = new String[] {
                "settings", "grades", "enrollments", "sections", "courses", "instructors", "students"
        };

        Connection c = null;
        Statement st = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);
            st = c.createStatement();

            for (String t : tablesInOrder) {
                String bt = "ERP_DB." + t + "_backup_" + tag;
                String checkSql = "SELECT 1 FROM " + bt + " LIMIT 1";
                try (ResultSet rs = st.executeQuery(checkSql)) {}
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String t : tablesInOrder) {
                String bt = t + "_backup_" + tag;
                st.executeUpdate("TRUNCATE TABLE ERP_DB." + t);
                st.executeUpdate("INSERT INTO ERP_DB." + t + " SELECT * FROM ERP_DB." + bt);
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
            c.commit();
            return true;
        } catch (SQLException ex) {
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
            throw ex;
        } finally {
            if (st != null) try { st.close(); } catch (SQLException ignore) {}
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignore) {}
        }
    }

    /**
     * Delete a course ONLY if NO students are enrolled in any of its sections.
     * - If any enrollment exists → returns false (do not delete).
     * - Else: deletes sections of that course and then the course row.
     */
    public boolean safeDeleteCourse(int courseId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // Check if any section has students
            String check = """
                SELECT COUNT(*) AS cnt
                FROM ERP_DB.enrollments en
                JOIN ERP_DB.sections s ON en.section_id = s.section_id
                WHERE s.course_id = ?
                """;
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setInt(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        c.rollback();
                        return false; // cannot delete
                    }
                }
            }

            // Delete all (empty) sections for this course
            String delSections = "DELETE FROM ERP_DB.sections WHERE course_id = ?";
            try (PreparedStatement ps = c.prepareStatement(delSections)) {
                ps.setInt(1, courseId);
                ps.executeUpdate();
            }

            // Delete course
            String delCourse = "DELETE FROM ERP_DB.courses WHERE course_id = ?";
            try (PreparedStatement ps = c.prepareStatement(delCourse)) {
                ps.setInt(1, courseId);
                ps.executeUpdate();
            }

            c.commit();
            return true;
        }
    }

    /**
     * Delete a section ONLY if it has NO enrollments.
     * Returns:
     * - true  => section deleted
     * - false => section has students, cannot delete
     */
    public boolean safeDeleteSection(int sectionId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // Check enrollments for this section
            String check = "SELECT COUNT(*) AS cnt FROM ERP_DB.enrollments WHERE section_id = ?";
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        c.rollback();
                        return false; // cannot delete
                    }
                }
            }

            // No enrollments → safe to delete
            String delSec = "DELETE FROM ERP_DB.sections WHERE section_id = ?";
            try (PreparedStatement ps = c.prepareStatement(delSec)) {
                ps.setInt(1, sectionId);
                ps.executeUpdate();
            }

            c.commit();
            return true;
        }
    }

    /**
     * Delete a user safely:
     * - For student: block if they have any enrollments; else delete from students then users_auth
     * - For instructor: block if they own any sections; else delete from instructors then users_auth
     * - For admin/other: just delete from users_auth (you should prevent self-delete in UI)
     *
     * Returns:
     * - true  => deleted
     * - false => blocked due to enrollments/sections or user not found
     */
    public boolean safeDeleteUser(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // 1) Find role
            String role = null;
            String roleSql = "SELECT role FROM Auth_DB.users_auth WHERE user_id = ?";
            try (PreparedStatement ps = c.prepareStatement(roleSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        role = rs.getString("role");
                    } else {
                        c.rollback();
                        return false; // no such user
                    }
                }
            }

            role = role == null ? "" : role.toLowerCase().trim();

            if ("student".equals(role)) {
                // Check enrollments
                String checkEnr = "SELECT COUNT(*) AS cnt FROM ERP_DB.enrollments WHERE student_id = ?";
                try (PreparedStatement ps = c.prepareStatement(checkEnr)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt("cnt") > 0) {
                            c.rollback();
                            return false; // has enrollments
                        }
                    }
                }

                // Delete from students
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM ERP_DB.students WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            } else if ("instructor".equals(role)) {
                // Check sections owned
                String checkSec = "SELECT COUNT(*) AS cnt FROM ERP_DB.sections WHERE instructor_id = ?";
                try (PreparedStatement ps = c.prepareStatement(checkSec)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt("cnt") > 0) {
                            c.rollback();
                            return false; // has sections
                        }
                    }
                }

                // Delete from instructors
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM ERP_DB.instructors WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            } else {
                // admin or other role: no extra checks here
                // UI should block deleting current logged-in admin
            }

            // Finally, delete from Auth_DB
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM Auth_DB.users_auth WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            c.commit();
            return true;
        }
    }

}
