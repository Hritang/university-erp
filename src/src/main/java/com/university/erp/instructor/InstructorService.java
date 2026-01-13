package com.university.erp.instructor;

import com.university.erp.data.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class InstructorService {

    private final InstructorDAO dao = new InstructorDAO();


    public static class ClassStats {
        public final double avg, min, max, stddev;
        public final int n;

        public Map<String, Integer> distribution = new LinkedHashMap<>();

        public ClassStats(double avg, double min, double max, double stddev, int n) {
            this.avg = avg;
            this.min = min;
            this.max = max;
            this.stddev = stddev;
            this.n = n;


            distribution.put("A", 0);
            distribution.put("B", 0);
            distribution.put("C", 0);
            distribution.put("D", 0);
            distribution.put("F", 0);
        }
    }


    private Optional<Integer> getUserId(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return Optional.empty();
            return dao.getUserIdByUsername(conn, username);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public String getMaintenanceBanner() {
        try (Connection c = DBConnection.getConnection()) {
            Optional<String> onOpt = dao.getSetting(c, "maintenance_on");
            boolean isOn = onOpt.isPresent() && "true".equalsIgnoreCase(onOpt.get());

            if (isOn) {
                return dao.getSetting(c, "maintenance_banner").orElse("System under maintenance");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



    public List<InstructorDAO.SectionView> mySections(String username, String semester, Integer year) {
        Optional<Integer> opt = getUserId(username);
        if (opt.isEmpty()) return new ArrayList<>();
        int instructorId = opt.get();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return new ArrayList<>();
            return dao.listSectionsForInstructor(conn, instructorId, semester, year);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean isInstructorForSection(String username, int sectionId) {
        Optional<Integer> opt = getUserId(username);
        if (opt.isEmpty()) return false;
        int instructorId = opt.get();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            return dao.isInstructorForSection(conn, instructorId, sectionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public List<InstructorDAO.EnrollmentRow> getEnrollments(int sectionId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return new ArrayList<>();
            return dao.listEnrollmentsForSection(conn, sectionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<Integer, Map<String, Double>> getSectionScores(int sectionId) {
        Map<Integer, Map<String, Double>> out = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return out;

            List<InstructorDAO.EnrollmentRow> rows = dao.listEnrollmentsForSection(conn, sectionId);
            for (var r : rows) {
                Map<String, Double> sc = dao.getScoresForEnrollment(conn, r.enrollmentId);
                out.put(r.enrollmentId, sc);
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return out;
        }
    }

    public boolean saveScoresBulk(String username, int sectionId, Map<Integer, Map<String, Double>> payload) {

        if (!isInstructorForSection(username, sectionId)) {
            System.err.println("[InstructorService] saveScoresBulk: Not your section.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;


            Optional<String> mOpt = dao.getSetting(conn, "maintenance_on");
            if (mOpt.isPresent() && mOpt.get().equalsIgnoreCase("true")) {
                System.err.println("System is in Maintenance Mode. Save rejected.");
                return false;
            }

            conn.setAutoCommit(false);
            try {
                for (var entry : payload.entrySet()) {
                    int enrollmentId = entry.getKey();
                    Map<String, Double> scores = entry.getValue();
                    for (var compEntry : scores.entrySet()) {
                        String comp = compEntry.getKey();
                        double score = compEntry.getValue();
                        dao.upsertScore(conn, enrollmentId, comp, score);
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private String numericToLetter(double numeric) {
        if (numeric >= 90) return "A";
        if (numeric >= 80) return "B";
        if (numeric >= 70) return "C";
        if (numeric >= 60) return "D";
        return "F";
    }

    public Optional<ClassStats> computeFinalsForSection(String username, int sectionId, int wQuiz, int wMidterm, int wEndSem) {
        if (!isInstructorForSection(username, sectionId)) {
            return Optional.empty();
        }

        int sum = wQuiz + wMidterm + wEndSem;
        if (sum != 100) {
            System.err.println("[InstructorService] Weights must sum to 100.");
            return Optional.empty();
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return Optional.empty();


            Optional<String> mOpt = dao.getSetting(conn, "maintenance_on");
            if (mOpt.isPresent() && mOpt.get().equalsIgnoreCase("true")) {
                System.err.println("System is in Maintenance Mode. Computation rejected.");
                return Optional.empty();
            }


            List<InstructorDAO.EnrollmentRow> rows = dao.listEnrollmentsForSection(conn, sectionId);
            if (rows.isEmpty()) return Optional.empty();

            List<Double> finals = new ArrayList<>();
            List<String> letters = new ArrayList<>();

            conn.setAutoCommit(false);
            try {
                for (var r : rows) {
                    Map<String, Double> scores = dao.getScoresForEnrollment(conn, r.enrollmentId);
                    double quiz   = scores.getOrDefault("Quiz",    0.0);
                    double mid    = scores.getOrDefault("Midterm", 0.0);
                    double end    = scores.getOrDefault("EndSem",  0.0);

                    double numeric = quiz   * (wQuiz    / 100.0)
                            + mid    * (wMidterm / 100.0)
                            + end    * (wEndSem  / 100.0);

                    String letter = numericToLetter(numeric);


                    dao.upsertFinal(conn, r.enrollmentId, numeric, letter);

                    finals.add(numeric);
                    letters.add(letter);
                }


                double avg = finals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double min = finals.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double max = finals.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double var = 0.0;
                for (double v : finals) var += (v - avg) * (v - avg);
                var = finals.size() > 0 ? var / (finals.size() - 1) : 0.0; // Sample std dev
                double sd = Math.sqrt(var);

                conn.commit();


                ClassStats stats = new ClassStats(avg, min, max, sd, finals.size());
                for(String l : letters) {
                    stats.distribution.put(l, stats.distribution.getOrDefault(l, 0) + 1);
                }

                return Optional.of(stats);

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return Optional.empty();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}