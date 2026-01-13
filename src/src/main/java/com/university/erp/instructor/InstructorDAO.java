package com.university.erp.instructor;

import java.sql.*;
import java.util.*;

public class InstructorDAO {



    public static class SectionView {
        public final int sectionId;
        public final String courseCode, courseTitle;
        public final String dayTime, room, semester;
        public final int year, capacity;

        public SectionView(int sectionId, String courseCode, String courseTitle,
                           String dayTime, String room, String semester,
                           int year, int capacity) {
            this.sectionId = sectionId;
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.dayTime = dayTime;
            this.room = room;
            this.semester = semester;
            this.year = year;
            this.capacity = capacity;
        }
    }

    public static class EnrollmentRow {
        public final int enrollmentId;
        public final int studentId;
        public final String username;
        public final String rollNo;

        public EnrollmentRow(int enrollmentId, int studentId,
                             String username, String rollNo) {
            this.enrollmentId = enrollmentId;
            this.studentId = studentId;
            this.username = username;
            this.rollNo = rollNo;
        }
    }




    public Optional<Integer> getUserIdByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT user_id FROM Auth_DB.users_auth WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getInt("user_id"));
                return Optional.empty();
            }
        }
    }
    public Optional<String> getSetting(Connection conn, String key) throws SQLException {
        String sql = "SELECT value FROM ERP_DB.settings WHERE key_ = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString("value"));
                return Optional.empty();
            }
        }
    }


    public List<SectionView> listSectionsForInstructor(Connection conn,
                                                       int instructorId,
                                                       String semester,
                                                       Integer year) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT s.section_id, c.code AS courseCode, c.title AS courseTitle, " +
                        "       s.day_time, s.room, s.semester, s.year, s.capacity " +
                        "FROM ERP_DB.sections s " +
                        "JOIN ERP_DB.courses c ON s.course_id = c.course_id " +
                        "WHERE s.instructor_id = ? "
        );
        if (semester != null) sb.append(" AND s.semester = ? ");
        if (year != null)     sb.append(" AND s.year = ? ");
        sb.append(" ORDER BY s.year DESC, s.semester, c.code, s.section_id");

        String sql = sb.toString();
        List<SectionView> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, instructorId);
            if (semester != null) ps.setString(idx++, semester);
            if (year != null)     ps.setInt(idx++, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SectionView(
                            rs.getInt("section_id"),
                            rs.getString("courseCode"),
                            rs.getString("courseTitle"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("semester"),
                            rs.getInt("year"),
                            rs.getInt("capacity")
                    ));
                }
            }
        }
        return out;
    }


    public boolean isInstructorForSection(Connection conn, int instructorId, int sectionId) throws SQLException {
        String sql = "SELECT 1 FROM ERP_DB.sections WHERE section_id = ? AND instructor_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, instructorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }



    public List<EnrollmentRow> listEnrollmentsForSection(Connection conn, int sectionId) throws SQLException {
        String sql =
                "SELECT e.enrollment_id, st.user_id AS student_id, ua.username, st.roll_no " +
                        "FROM ERP_DB.enrollments e " +
                        "JOIN ERP_DB.students st ON e.student_id = st.user_id " +
                        "LEFT JOIN Auth_DB.users_auth ua ON ua.user_id = st.user_id " +
                        "WHERE e.section_id = ? AND e.status = 'active' " +
                        "ORDER BY ua.username";
        List<EnrollmentRow> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new EnrollmentRow(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getString("username"),
                            rs.getString("roll_no")
                    ));
                }
            }
        }
        return out;
    }


    public Map<String, Double> getScoresForEnrollment(Connection conn, int enrollmentId) throws SQLException {
        String sql = "SELECT component, score FROM ERP_DB.grades WHERE enrollment_id = ?";
        Map<String, Double> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("component"), rs.getDouble("score"));
                }
            }
        }
        return out;
    }


    public boolean upsertScore(Connection conn, int enrollmentId, String component, double score) throws SQLException {

        String update = "UPDATE ERP_DB.grades SET score = ?, final_grade = NULL " +
                "WHERE enrollment_id = ? AND component = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setDouble(1, score);
            ps.setInt(2, enrollmentId);
            ps.setString(3, component);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
        }


        String insert = "INSERT INTO ERP_DB.grades (enrollment_id, component, score) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setDouble(3, score);
            return ps.executeUpdate() == 1;
        }
    }


    public boolean upsertFinal(Connection conn, int enrollmentId, double numericFinal, String letter) throws SQLException {
        String update = "UPDATE ERP_DB.grades SET score = ?, final_grade = ? " +
                "WHERE enrollment_id = ? AND component = 'FINAL'";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setDouble(1, numericFinal);
            ps.setString(2, letter);
            ps.setInt(3, enrollmentId);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
        }

        String insert = "INSERT INTO ERP_DB.grades (enrollment_id, component, score, final_grade) " +
                "VALUES (?, 'FINAL', ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, enrollmentId);
            ps.setDouble(2, numericFinal);
            ps.setString(3, letter);
            return ps.executeUpdate() == 1;
        }
    }
}
