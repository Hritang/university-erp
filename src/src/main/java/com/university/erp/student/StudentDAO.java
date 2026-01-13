package com.university.erp.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class StudentDAO {



    public static class CourseView {
        public final int id;
        public final String code, title, instructor;
        public final int credits, totalCapacity;

        public CourseView(int id, String code, String title, int credits, int totalCapacity, String instructor) {
            this.id = id; this.code = code; this.title = title; this.credits = credits;
            this.totalCapacity = totalCapacity; this.instructor = instructor;
        }
    }

    public static class RegView {
        public final int sectionId;
        public final String courseCode;
        public final String courseTitle;
        public final String instructor;
        public RegView(int sectionId, String courseCode, String courseTitle, String instructor) {
            this.sectionId = sectionId; this.courseCode = courseCode; this.courseTitle = courseTitle; this.instructor = instructor;
        }
    }

    public static class TimeView {
        public final String course, dayTime, room;
        public TimeView(String course, String dayTime, String room) {
            this.course = course; this.dayTime = dayTime; this.room = room;
        }
    }

    public static class GradeView {
        public final String code, title, component, finalGrade;
        public final double score;
        public GradeView(String code, String title, String component, double score, String finalGrade) {
            this.code = code; this.title = title; this.component = component; this.score = score; this.finalGrade = finalGrade;
        }
    }

    public static class SectionInfo {
        public final int sectionId, capacity;
        public final String instructor, dayTime, room;
        public SectionInfo(int sectionId, String instructor, String dayTime, String room, int capacity) {
            this.sectionId = sectionId; this.instructor = instructor;
            this.dayTime = dayTime; this.room = room; this.capacity = capacity;
        }
    }



    public Optional<Integer> getUserIdByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT user_id FROM Auth_DB.users_auth WHERE username = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt("user_id")) : Optional.empty();
            }
        }
    }

    public boolean isStudent(Connection conn, int userId) throws SQLException {
        String sql = "SELECT 1 FROM ERP_DB.students WHERE user_id = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isAlreadyInSection(Connection conn, int studentId, int sectionId) throws SQLException {
        String sql = "SELECT 1 FROM ERP_DB.enrollments WHERE student_id=? AND section_id=? AND status='active'";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            p.setInt(2, sectionId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<SectionInfo> getSections(Connection conn, int courseId) throws SQLException {
        String sql = "SELECT s.section_id, ua.username AS instructor, s.day_time, s.room, s.capacity " +
                "FROM ERP_DB.sections s LEFT JOIN Auth_DB.users_auth ua ON s.instructor_id = ua.user_id " +
                "WHERE s.course_id = ?";
        List<SectionInfo> out = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, courseId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next())
                    out.add(new SectionInfo(rs.getInt("section_id"),
                            rs.getString("instructor"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity")));
            }
        }
        return out;
    }


    public List<CourseView> listCatalog(Connection conn) throws SQLException {
        String sql =
                "SELECT c.course_id, c.code, c.title, c.credits, " +
                        "       COALESCE(SUM(s.capacity),0) AS total_capacity, " +
                        "       COALESCE(GROUP_CONCAT(DISTINCT ua.username SEPARATOR ', '),'--') AS instructor_list " +
                        "FROM ERP_DB.courses c " +
                        "LEFT JOIN ERP_DB.sections s ON c.course_id = s.course_id " +
                        "LEFT JOIN Auth_DB.users_auth ua ON s.instructor_id = ua.user_id " +
                        "GROUP BY c.course_id, c.code, c.title, c.credits " +
                        "ORDER BY c.code";
        List<CourseView> out = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                out.add(new CourseView(
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits"),
                        rs.getInt("total_capacity"),
                        rs.getString("instructor_list")
                ));
            }
        }
        return out;
    }



    public List<Integer> findAvailableSectionIdsForCourse(Connection conn, int courseId) throws SQLException {
        String sql =
                "SELECT s.section_id, s.capacity, COUNT(e.enrollment_id) AS enrolled " +
                        "FROM ERP_DB.sections s " +
                        "LEFT JOIN ERP_DB.enrollments e ON s.section_id = e.section_id AND e.status='active' " +
                        "WHERE s.course_id = ? " +
                        "GROUP BY s.section_id, s.capacity " +
                        "HAVING enrolled < s.capacity " +
                        "ORDER BY s.section_id";
        List<Integer> out = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, courseId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) out.add(rs.getInt("section_id"));
            }
        }
        return out;
    }

    public boolean isSectionFull(Connection conn, int sectionId) throws SQLException {
        String sql =
                "SELECT s.capacity, COUNT(e.enrollment_id) AS enrolled " +
                        "FROM ERP_DB.sections s " +
                        "LEFT JOIN ERP_DB.enrollments e ON s.section_id = e.section_id AND e.status='active' " +
                        "WHERE s.section_id = ? " +
                        "GROUP BY s.capacity";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, sectionId);
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int cap = rs.getInt("capacity");
                int enrolled = rs.getInt("enrolled");
                return enrolled >= cap;
            }
        }
    }

    public List<RegView> listRegistered(Connection conn, int studentId) throws SQLException {
        String sql =
                "SELECT s.section_id, c.code AS courseCode, c.title AS courseTitle, ua.username AS instructor " +
                        "FROM ERP_DB.enrollments e " +
                        "JOIN ERP_DB.sections s ON e.section_id = s.section_id " +
                        "JOIN ERP_DB.courses c ON s.course_id = c.course_id " +
                        "LEFT JOIN Auth_DB.users_auth ua ON s.instructor_id = ua.user_id " +
                        "WHERE e.student_id = ? AND e.status='active' " +
                        "ORDER BY c.code";
        List<RegView> out = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) out.add(new RegView(
                        rs.getInt("section_id"),
                        rs.getString("courseCode"),
                        rs.getString("courseTitle"),
                        rs.getString("instructor")
                ));
            }
        }
        return out;
    }

    public boolean dropEnrollment(Connection conn, int studentId, int sectionId) throws SQLException {
        String sql = "DELETE FROM ERP_DB.enrollments WHERE student_id = ? AND section_id = ? AND status='active'";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            p.setInt(2, sectionId);
            return p.executeUpdate() > 0;
        }
    }

    public boolean addEnrollment(Connection conn, int studentId, int sectionId) throws SQLException {
        String sql = "INSERT INTO ERP_DB.enrollments (student_id, section_id, status) VALUES (?, ?, 'active')";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            p.setInt(2, sectionId);
            return p.executeUpdate() == 1;
        }
    }

    public boolean alreadyEnrolledInCourse(Connection conn, int studentId, int courseId) throws SQLException {
        String sql =
                "SELECT 1 FROM ERP_DB.enrollments e " +
                        "JOIN ERP_DB.sections s ON e.section_id = s.section_id " +
                        "WHERE e.student_id = ? AND s.course_id = ? AND e.status='active' LIMIT 1";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            p.setInt(2, courseId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<TimeView> getSectionTimeInfo(Connection conn, int sectionId) throws SQLException {
        String sql =
                "SELECT c.title, s.day_time, s.room " +
                        "FROM ERP_DB.sections s JOIN ERP_DB.courses c ON s.course_id = c.course_id " +
                        "WHERE s.section_id = ?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, sectionId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next()
                        ? Optional.of(new TimeView(rs.getString("title"), rs.getString("day_time"), rs.getString("room")))
                        : Optional.empty();
            }
        }
    }

    public List<TimeView> getStudentSectionTimes(Connection conn, int studentId) throws SQLException {
        String sql =
                "SELECT c.title, s.day_time, s.room " +
                        "FROM ERP_DB.enrollments e " +
                        "JOIN ERP_DB.sections s ON e.section_id = s.section_id " +
                        "JOIN ERP_DB.courses c ON s.course_id = c.course_id " +
                        "WHERE e.student_id = ? AND e.status='active'";
        List<TimeView> out = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    out.add(new TimeView(rs.getString("title"), rs.getString("day_time"), rs.getString("room")));
                }
            }
        }
        return out;
    }

    public List<TimeView> getTimetable(Connection conn, int studentId) throws SQLException {
        return getStudentSectionTimes(conn, studentId);
    }


    public Optional<String> getSetting(Connection conn, String key) throws SQLException {
        String sql = "SELECT value FROM ERP_DB.settings WHERE key_=?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, key);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("value")) : Optional.empty();
            }
        }
    }


    public List<GradeView> listGrades(Connection conn, int studentId) throws SQLException {
        String sql =
                "SELECT c.code, c.title, g.component, g.score, g.final_grade " +
                        "FROM ERP_DB.enrollments e " +
                        "JOIN ERP_DB.sections s ON e.section_id = s.section_id " +
                        "JOIN ERP_DB.courses c ON s.course_id = c.course_id " +
                        "JOIN ERP_DB.grades g ON g.enrollment_id = e.enrollment_id " +
                        "WHERE e.student_id = ?";
        List<GradeView> out = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    out.add(new GradeView(
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("component"),
                            rs.getDouble("score"),
                            rs.getString("final_grade")
                    ));
                }
            }
        }
        return out;
    }

}

