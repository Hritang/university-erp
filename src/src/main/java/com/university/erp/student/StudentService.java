package com.university.erp.student;

import com.university.erp.data.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class StudentService {

    private final StudentDAO dao = new StudentDAO();

    private Optional<Integer> getUserId(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[StudentService] getUserId: connection is NULL");
                return Optional.empty();
            }
            return dao.getUserIdByUsername(conn, username);
        } catch (SQLException e) {
            System.err.println("[StudentService] getUserId SQL error:");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private boolean isStudent(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[StudentService] isStudent: connection is NULL");
                return false;
            }
            return dao.isStudent(conn, userId);
        } catch (SQLException e) {
            System.err.println("[StudentService] isStudent SQL error:");
            e.printStackTrace();
            return false;
        }
    }


    public List<StudentDAO.CourseView> viewCatalog() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[StudentService] viewCatalog: connection is NULL");
                return new ArrayList<>();
            }
            return dao.listCatalog(conn);
        } catch (SQLException ex) {
            System.err.println("[StudentService] viewCatalog SQL error:");
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<StudentDAO.RegView> viewRegistered(String username) {
        Optional<Integer> optId = getUserId(username);
        if (optId.isEmpty()) {
            System.err.println("[StudentService] viewRegistered: no user_id for username=" + username);
            return new ArrayList<>();
        }
        int userId = optId.get();
        if (!isStudent(userId)) {
            System.err.println("[StudentService] viewRegistered: user_id=" + userId + " is NOT a student");
            return new ArrayList<>();
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[StudentService] viewRegistered: connection is NULL");
                return new ArrayList<>();
            }
            return dao.listRegistered(conn, userId);
        } catch (SQLException e) {
            System.err.println("[StudentService] viewRegistered SQL error:");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<StudentDAO.TimeView> viewTimetable(String username) {
        Optional<Integer> optId = getUserId(username);
        if (optId.isEmpty()) return new ArrayList<>();
        int userId = optId.get();
        if (!isStudent(userId)) return new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[StudentService] viewTimetable: connection is NULL");
                return new ArrayList<>();
            }
            return dao.getTimetable(conn, userId);
        } catch (SQLException e) {
            System.err.println("[StudentService] viewTimetable SQL error:");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<StudentDAO.GradeView> viewGrades(String username) {
        Optional<Integer> optId = getUserId(username);
        if (optId.isEmpty()) return new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[StudentService] viewGrades: connection is NULL");
                return new ArrayList<>();
            }
            return dao.listGrades(conn, optId.get());
        } catch (SQLException e) {
            System.err.println("[StudentService] viewGrades SQL error:");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<StudentDAO.SectionInfo> getSections(int courseId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return new ArrayList<>();
            return dao.getSections(conn, courseId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }



    public static class RegisterResult {
        public final boolean success;
        public final String message;

        public RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public RegisterResult registerCourseInSection(String username, int sectionId) {
        System.out.println("[StudentService] registerCourseInSection username=" + username + ", sectionId=" + sectionId);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new RegisterResult(false, "Internal database connection error.");
            }

            Optional<Integer> opt = dao.getUserIdByUsername(conn, username);
            if (opt.isEmpty()) {
                return new RegisterResult(false, "User account not found.");
            }
            int studentId = opt.get();

            if (!dao.isStudent(conn, studentId)) {
                return new RegisterResult(false, "Only students can register for courses.");
            }

            Optional<String> mOpt = dao.getSetting(conn, "maintenance_on");
            if (mOpt.isPresent() && mOpt.get().equalsIgnoreCase("true")) {
                conn.rollback();
                return new RegisterResult(false, "Registration is disabled due to system maintenance.");
            }

            try {
                Optional<String> rdOpt = dao.getSetting(conn, "registration_deadline");
                if (rdOpt.isPresent()) {
                    LocalDate deadline = LocalDate.parse(rdOpt.get()); // expects YYYY-MM-DD
                    LocalDate today = LocalDate.now();
                    if (today.isAfter(deadline)) {
                        return new RegisterResult(false, "Registration is closed. Deadline was: " + deadline);
                    }
                }
            } catch (Exception ex) {
                System.err.println("[registerCourseInSection] error parsing registration_deadline");
                ex.printStackTrace();
            }



            conn.setAutoCommit(false);
            try {
                if (dao.isAlreadyInSection(conn, studentId, sectionId)) {
                    conn.rollback();
                    return new RegisterResult(false, "You are already registered in this section.");
                }

                int courseId;
                String candidateTime = null;

                String sql = "SELECT course_id, day_time FROM ERP_DB.sections WHERE section_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, sectionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new RegisterResult(false, "Selected section not found.");
                        }
                        courseId = rs.getInt("course_id");
                        candidateTime = rs.getString("day_time");
                    }
                }

                if (dao.alreadyEnrolledInCourse(conn, studentId, courseId)) {
                    conn.rollback();
                    return new RegisterResult(false, "You are already registered in another section of this course.");
                }

                if (dao.isSectionFull(conn, sectionId)) {
                    conn.rollback();
                    return new RegisterResult(false, "Section is full.");
                }

                List<StudentDAO.TimeView> currentTimes = dao.getStudentSectionTimes(conn, studentId);
                if (clashesWithAny(candidateTime, currentTimes)) {
                    conn.rollback();
                    return new RegisterResult(false, "This section clashes with your existing timetable.");
                }

                if (!dao.addEnrollment(conn, studentId, sectionId)) {
                    conn.rollback();
                    return new RegisterResult(false, "Failed to save registration.");
                }

                conn.commit();
                return new RegisterResult(true, "Registered successfully!");

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return new RegisterResult(false, "Database error while registering.");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new RegisterResult(false, "Unexpected error while registering.");
        }
    }

    private boolean clashesWithAny(String candidateDayTime, List<StudentDAO.TimeView> times) {
        if (candidateDayTime == null) return false;

        String normalizedCandidate = normalizeTime(candidateDayTime);

        for (StudentDAO.TimeView t : times) {
            if (t.dayTime == null) continue;
            String normalizedExisting = normalizeTime(t.dayTime);
            if (normalizedCandidate.equals(normalizedExisting)) {
                return true; // clash found
            }
        }
        return false;
    }

    private String normalizeTime(String s) {
        return s.toLowerCase()
                .trim()
                .replace("–", "-")   // Replace EN-DASH (U+2013)
                .replace("—", "-")   // Replace EM-DASH (U+2014)
                .replace("−", "-")   // Replace MINUS SIGN (U+2212) just in case
                .replaceAll("\\s+", " "); // Remove repeated spaces
    }
    public String getMaintenanceBanner() {
        try (java.sql.Connection c = com.university.erp.data.DBConnection.getConnection()) {
            String sql = "SELECT key_, value FROM ERP_DB.settings WHERE key_ IN ('maintenance_on', 'maintenance_banner')";
            boolean isOn = false;
            String banner = "";

            try (java.sql.PreparedStatement ps = c.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("key_");
                    String val = rs.getString("value");
                    if ("maintenance_on".equals(key)) isOn = "true".equalsIgnoreCase(val);
                    if ("maintenance_banner".equals(key)) banner = val;
                }
            }

            return isOn ? banner : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // UPDATED dropCourse to return specific error messages
    public String dropCourse(String username, int sectionId) {
        System.out.println("[StudentService] dropCourse username=" + username + ", sectionId=" + sectionId);

        Optional<Integer> optId = getUserId(username);
        if (optId.isEmpty()) return "User not found.";

        int studentId = optId.get();
        if (!isStudent(studentId)) return "Access denied: Not a student.";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "Database connection failed.";

            // 1. Check Maintenance (Inside the same connection)
            Optional<String> mOpt = dao.getSetting(conn, "maintenance_on");
            if (mOpt.isPresent() && "true".equalsIgnoreCase(mOpt.get())) {
                return "Maintenance Mode is ON. Drops are currently disabled.";
            }

            // 2. Check Deadline
            try {
                Optional<String> ddOpt = dao.getSetting(conn, "drop_deadline");
                if (ddOpt.isPresent()) {
                    LocalDate deadline = LocalDate.parse(ddOpt.get());
                    if (LocalDate.now().isAfter(deadline)) {
                        return "Drop deadline passed on " + deadline;
                    }
                }
            } catch (Exception e) {
                System.err.println("Date parse error: " + e.getMessage());
                // Proceed cautiously or block? Usually allow if date is broken, or block.
            }

            // 3. Perform Drop
            boolean ok = dao.dropEnrollment(conn, studentId, sectionId);
            if (ok) {
                return "SUCCESS";
            } else {
                return "Could not drop course (DB Error or not enrolled).";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "System error: " + e.getMessage();
        }
    }
}
