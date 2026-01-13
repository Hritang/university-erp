package com.university.erp.ui.dashboard;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.university.erp.instructor.InstructorDAO;
import com.university.erp.instructor.InstructorService;
import com.university.erp.data.DBConnection;
import com.university.erp.ui.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
// --- FIX: THESE IMPORTS WERE MISSING ---
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// ---------------------------------------
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class InstructorDashboardFrame extends JFrame {

    // --- UI COLORS & THEME ---
    private static final Color BG_DARK_NAVY = new Color(35, 35, 60);
    private static final Color SIDEBAR_BG = new Color(45, 45, 75);
    private static final Color CARD_PURPLE = new Color(120, 130, 235);
    private static final Color CARD_WHITE = new Color(240, 240, 245);
    private static final Color TEXT_ON_PURPLE = Color.WHITE;
    private static final Color TEXT_ON_WHITE = new Color(40, 40, 60);

    private static final String WEIGHTS_FILE_PREFIX = "weights_";

    private final InstructorService service = new InstructorService();
    private final String username;

    // Tables
    private final JTable sectionsTable = new JTable();
    private final JTable gradeTable = new JTable();

    // Layout
    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerPanel = new JPanel(centerLayout);

    // Banner
    private final JPanel bannerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JLabel bannerLabel = new JLabel();

    // Currently opened section in gradebook
    private Integer currentSectionId = null;

    // Helper for Final grades
    private static class FinalPair {
        final Double numeric;
        final String letter;
        FinalPair(Double numeric, String letter) {
            this.numeric = numeric;
            this.letter = letter;
        }
    }

    public InstructorDashboardFrame(String username) {
        super("Instructor Portal");
        this.username = username;
        installLookAndFeel();

        centerPanel.setBackground(BG_DARK_NAVY);

        buildUI();
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            UIManager.put("Component.arc", 15);
            UIManager.put("Button.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception ignored) {}
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());

        // --- MAINTENANCE BANNER ---
        bannerPanel.setBackground(new Color(220, 53, 69)); // Red
        bannerPanel.setVisible(false);
        bannerLabel.setForeground(Color.WHITE);
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        bannerPanel.add(new JLabel("⚠ SYSTEM MAINTENANCE: "));
        bannerPanel.add(bannerLabel);
        getContentPane().add(bannerPanel, BorderLayout.NORTH);

        // ----- SIDEBAR -----
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));
        sidebar.setPreferredSize(new Dimension(240, 800));

        JLabel title = new JLabel("Instructor ERP");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(50));

        sidebar.add(makeSidebarButton("Dashboard", "🏠", () -> centerLayout.show(centerPanel, "home")));
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(makeSidebarButton("My Sections", "📘", () -> {
            centerLayout.show(centerPanel, "sections");
            loadSections();
        }));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            // Close current dashboard
            dispose();
            // Open login screen again
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        sidebar.add(logoutBtn);

        // ----- CENTER PANELS -----
        centerPanel.add(buildHomePanel(), "home");
        centerPanel.add(buildSectionsPanel(), "sections");
        centerPanel.add(buildGradePanel(), "grade");

        centerLayout.show(centerPanel, "home");

        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        checkMaintenance();
    }

    private void checkMaintenance() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                return service.getMaintenanceBanner();
            }
            @Override protected void done() {
                try {
                    String bannerText = get();
                    if (bannerText != null) {
                        bannerLabel.setText(bannerText);
                        bannerPanel.setVisible(true);
                    } else {
                        bannerPanel.setVisible(false);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private JButton makeSidebarButton(String text, String icon, Runnable onClick) {
        JButton btn = new JButton(icon + "   " + text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(new Color(200, 200, 220));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> { if (onClick != null) onClick.run(); });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(60, 60, 90));
                btn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(new Color(200, 200, 220));
            }
        });
        return btn;
    }

    // ================= HOME (Welcome) =================
    private JPanel buildHomePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 32));
        welcome.setForeground(Color.WHITE);
        wrapper.add(welcome, BorderLayout.NORTH);

        // Grid: 1 Row, 3 Columns
        JPanel grid = new JPanel(new GridLayout(1, 3, 30, 30));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(40, 0, 40, 0));

        // Card 1: My Sections
        grid.add(new DashboardCard("My Sections", "📘", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            centerLayout.show(centerPanel, "sections");
            loadSections();
        }));

        // Card 2: Student List (Pop up)
        grid.add(new DashboardCard("Student List", "👥", CARD_WHITE, TEXT_ON_WHITE, () -> {
            askForSectionAndShow("View Students", this::showStudentListDialog);
        }));

        // Card 3: Class Stats (Pop up)
        grid.add(new DashboardCard("Class Stats", "📊", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            askForSectionAndShow("View Stats", this::showClassStatsDialog);
        }));

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid, BorderLayout.NORTH);

        wrapper.add(gridWrapper, BorderLayout.CENTER);
        return wrapper;
    }

    // ================= MY SECTIONS =================
    private JPanel buildSectionsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel t = new JLabel("My Sections");
        t.setFont(new Font("SansSerif", Font.BOLD, 24));
        t.setForeground(Color.WHITE);
        p.add(t, BorderLayout.NORTH);

        sectionsTable.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"ID", "Code", "Title", "Day/Time", "Room", "Semester", "Year"}) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });

        sectionsTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedSection();
            }
        });

        JScrollPane scroll = new JScrollPane(sectionsTable);
        p.add(scroll, BorderLayout.CENTER);

        // BOTTOM PANEL WITH BUTTON
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton openBtn = new JButton("Open Gradebook");
        openBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        openBtn.setPreferredSize(new Dimension(150, 40));
        openBtn.addActionListener(e -> openSelectedSection());

        bottom.add(openBtn);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private void loadSections() {
        new SwingWorker<List<InstructorDAO.SectionView>, Void>() {
            @Override protected List<InstructorDAO.SectionView> doInBackground() {
                return service.mySections(username, null, null);
            }
            @Override protected void done() {
                try {
                    List<InstructorDAO.SectionView> list = get();
                    DefaultTableModel m = (DefaultTableModel) sectionsTable.getModel();
                    m.setRowCount(0);
                    for (InstructorDAO.SectionView s : list) {
                        m.addRow(new Object[]{s.sectionId, s.courseCode, s.courseTitle, s.dayTime, s.room, s.semester, s.year});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void openSelectedSection() {
        int row = sectionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a section from the table.");
            return;
        }
        int sectionId = (int) sectionsTable.getValueAt(row, 0);

        currentSectionId = sectionId;
        loadGradebook(sectionId);
        centerLayout.show(centerPanel, "grade");
    }

    // ================= GRADEBOOK PANEL =================
    private JPanel buildGradePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel t = new JLabel("Gradebook");
        t.setFont(new Font("SansSerif", Font.BOLD, 24));
        t.setForeground(Color.WHITE);

        JButton backBtn = new JButton("⬅ Back");
        backBtn.addActionListener(e -> {
            centerLayout.show(centerPanel, "sections");
            currentSectionId = null;
        });

        header.add(t, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        gradeTable.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"Enroll ID", "Student", "Roll", "Quiz", "Midterm", "EndSem", "Numeric Final", "Letter Final"}) {
            @Override public boolean isCellEditable(int row, int col) {
                return col >= 3 && col <= 5;
            }
        });

        JScrollPane scroll = new JScrollPane(gradeTable);
        p.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBtns.setOpaque(false);
        JButton exportCsv = new JButton("Export CSV");
        JButton importCsv = new JButton("Import CSV");
        exportCsv.addActionListener(e -> exportCsv());
        importCsv.addActionListener(e -> importCsv());
        leftBtns.add(exportCsv);
        leftBtns.add(importCsv);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBtns.setOpaque(false);
        JButton saveBtn = new JButton("💾 Save Scores");
        JButton finalBtn = new JButton("📌 Compute Final");
        saveBtn.addActionListener(e -> saveGrades());
        finalBtn.addActionListener(e -> computeFinal());
        rightBtns.add(saveBtn);
        rightBtns.add(finalBtn);

        bottom.add(leftBtns, BorderLayout.WEST);
        bottom.add(rightBtns, BorderLayout.EAST);

        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private void loadGradebook(int sectionId) {
        new SwingWorker<Void, Void>() {
            Map<Integer, Map<String, Double>> scoresMap;
            List<InstructorDAO.EnrollmentRow> students;
            Map<Integer, FinalPair> finalsMap;

            @Override protected Void doInBackground() {
                scoresMap = service.getSectionScores(sectionId);
                students  = service.getEnrollments(sectionId);
                finalsMap = fetchFinalsFor(students);
                return null;
            }

            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) gradeTable.getModel();
                    m.setRowCount(0);
                    for (InstructorDAO.EnrollmentRow st : students) {
                        Map<String, Double> sc = scoresMap.getOrDefault(st.enrollmentId, new HashMap<>());
                        double quiz = sc.getOrDefault("Quiz", 0.0);
                        double mid  = sc.getOrDefault("Midterm", 0.0);
                        double end  = sc.getOrDefault("EndSem", 0.0);

                        FinalPair fp = finalsMap.get(st.enrollmentId);
                        Double numericFinal = fp != null ? fp.numeric : null;
                        String letterFinal  = fp != null ? fp.letter  : null;

                        m.addRow(new Object[]{st.enrollmentId, st.username, st.rollNo, quiz, mid, end, numericFinal, letterFinal});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private Map<Integer, FinalPair> fetchFinalsFor(List<InstructorDAO.EnrollmentRow> students) {
        Map<Integer, FinalPair> map = new HashMap<>();
        if (students == null || students.isEmpty()) return map;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return map;
            StringBuilder sb = new StringBuilder("SELECT enrollment_id, score, final_grade FROM ERP_DB.grades WHERE component='FINAL' AND enrollment_id IN (");
            for (int i = 0; i < students.size(); i++) { if (i > 0) sb.append(','); sb.append('?'); }
            sb.append(')');

            try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
                int idx = 1;
                for (InstructorDAO.EnrollmentRow st : students) ps.setInt(idx++, st.enrollmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        map.put(rs.getInt("enrollment_id"), new FinalPair(rs.getDouble("score"), rs.getString("final_grade")));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // ================= ACTIONS =================
    private void saveGrades() {
        if (currentSectionId == null) return;
        int rows = gradeTable.getRowCount();
        Map<Integer, Map<String, Double>> payload = new HashMap<>();

        try {
            for (int i = 0; i < rows; i++) {
                int enrollmentId = (int) gradeTable.getValueAt(i, 0);
                String studentName = (String) gradeTable.getValueAt(i, 1);
                double quiz = parseScore(gradeTable.getValueAt(i, 3));
                double mid  = parseScore(gradeTable.getValueAt(i, 4));
                double end  = parseScore(gradeTable.getValueAt(i, 5));

                // VALIDATION
                if(quiz < 0 || quiz > 100 || mid < 0 || mid > 100 || end < 0 || end > 100) {
                    JOptionPane.showMessageDialog(this, "Invalid score for " + studentName + "\nMust be 0-100.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Map<String, Double> sc = new HashMap<>();
                sc.put("Quiz", quiz); sc.put("Midterm", mid); sc.put("EndSem", end);
                payload.put(enrollmentId, sc);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number in scores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (service.saveScoresBulk(username, currentSectionId, payload)) {
            JOptionPane.showMessageDialog(this, "Scores saved.");
            loadGradebook(currentSectionId);
        } else {
            JOptionPane.showMessageDialog(this, "Save failed. (Check if Maintenance Mode is ON)", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parseScore(Object val) throws NumberFormatException {
        if (val == null) return 0.0;
        String s = val.toString().trim();
        if (s.isEmpty()) return 0.0;
        return Double.parseDouble(s);
    }

    private void computeFinal() {
        if (currentSectionId == null) return;
        try {
            String qStr = JOptionPane.showInputDialog(this, "Quiz weight %:", "20"); if(qStr==null) return;
            String mStr = JOptionPane.showInputDialog(this, "Midterm weight %:", "30"); if(mStr==null) return;
            String eStr = JOptionPane.showInputDialog(this, "EndSem weight %:", "50"); if(eStr==null) return;

            int wQ = Integer.parseInt(qStr);
            int wM = Integer.parseInt(mStr);
            int wE = Integer.parseInt(eStr);

            if (wQ + wM + wE != 100) {
                JOptionPane.showMessageDialog(this, "Weights must sum to 100.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            var opt = service.computeFinalsForSection(username, currentSectionId, wQ, wM, wE);

            if (opt.isPresent()) {
                saveWeights(wQ, wM, wE);
                InstructorService.ClassStats s = opt.get();
                String msg = String.format("Grades computed!\nAvg: %.2f\nMax: %.2f", s.avg, s.max);
                JOptionPane.showMessageDialog(this, msg);
                loadGradebook(currentSectionId);
            } else {
                JOptionPane.showMessageDialog(this, "Computation failed. (Maintenance Mode might be ON)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid weights.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (currentSectionId == null) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
            fw.write("enrollment_id,username,roll,quiz,midterm,endsem,numeric_final,letter_final\n");
            for (int i = 0; i < gradeTable.getRowCount(); i++) {
                for (int j = 0; j < 8; j++) {
                    Object val = gradeTable.getValueAt(i, j);
                    fw.write((val == null ? "" : val.toString()) + (j < 7 ? "," : ""));
                }
                fw.write("\n");
            }
            JOptionPane.showMessageDialog(this, "Export successful.");
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void importCsv() {
        if (currentSectionId == null) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Map<Integer, Map<String, Double>> payload = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if(parts.length < 4) continue;

                int eid = Integer.parseInt(parts[0]);
                double q, m, e;

                if(parts.length == 4) {
                    q = Double.parseDouble(parts[1]);
                    m = Double.parseDouble(parts[2]);
                    e = Double.parseDouble(parts[3]);
                } else {
                    q = Double.parseDouble(parts[3]);
                    m = Double.parseDouble(parts[4]);
                    e = Double.parseDouble(parts[5]);
                }

                if (q < 0 || q > 100 || m < 0 || m > 100 || e < 0 || e > 100) {
                    JOptionPane.showMessageDialog(this, "Import Failed: Row " + eid + " contains scores outside 0-100.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Map<String, Double> sc = new HashMap<>();
                sc.put("Quiz", q); sc.put("Midterm", m); sc.put("EndSem", e);
                payload.put(eid, sc);
            }
            if(service.saveScoresBulk(username, currentSectionId, payload)){
                JOptionPane.showMessageDialog(this, "Import successful.");
                loadGradebook(currentSectionId);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage());
        }
    }

    // ================= WEIGHT PERSISTENCE =================
    private void saveWeights(int wQ, int wM, int wE) {
        String filename = WEIGHTS_FILE_PREFIX + username + ".txt";
        try (FileWriter fw = new FileWriter(filename)) { fw.write(wQ + "," + wM + "," + wE); }
        catch (IOException ex) { System.err.println("Error saving weights."); }
    }

    private Optional<int[]> loadWeights() {
        String filename = WEIGHTS_FILE_PREFIX + username + ".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int wQ = Integer.parseInt(parts[0].trim());
                    int wM = Integer.parseInt(parts[1].trim());
                    int wE = Integer.parseInt(parts[2].trim());
                    if (wQ + wM + wE == 100) return Optional.of(new int[]{wQ, wM, wE});
                }
            }
        } catch (Exception ex) { }
        return Optional.empty();
    }

    // ================= DIALOGS & CHARTS =================

    interface SectionAction { void run(int sectionId, String sectionName); }

    private void askForSectionAndShow(String title, SectionAction action) {
        SwingWorker<List<InstructorDAO.SectionView>, Void> w = new SwingWorker<>() {
            @Override protected List<InstructorDAO.SectionView> doInBackground() {
                return service.mySections(username, null, null);
            }
            @Override protected void done() {
                try {
                    List<InstructorDAO.SectionView> list = get();
                    if (list.isEmpty()) { JOptionPane.showMessageDialog(InstructorDashboardFrame.this, "No sections found."); return; }
                    SectionItem[] items = new SectionItem[list.size()];
                    for(int i=0; i<list.size(); i++) items[i] = new SectionItem(list.get(i).sectionId, list.get(i).courseCode + " - " + list.get(i).courseTitle);
                    SectionItem selected = (SectionItem) JOptionPane.showInputDialog(InstructorDashboardFrame.this, "Select Section:", title, JOptionPane.PLAIN_MESSAGE, null, items, items[0]);
                    if (selected != null) action.run(selected.id, selected.label);
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        w.execute();
    }

    private static class SectionItem {
        final int id; final String label;
        SectionItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private void showStudentListDialog(int sectionId, String sectionName) {
        JDialog d = new JDialog(this, "Students: " + sectionName, true);
        d.setSize(500, 400); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout());
        JTable t = new JTable();
        t.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Roll No", "Name"}));
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        SwingWorker<List<InstructorDAO.EnrollmentRow>, Void> w = new SwingWorker<>() {
            @Override protected List<InstructorDAO.EnrollmentRow> doInBackground() { return service.getEnrollments(sectionId); }
            @Override protected void done() {
                try { for(var r : get()) ((DefaultTableModel) t.getModel()).addRow(new Object[]{r.rollNo, r.username}); } catch(Exception e){}
            }
        };
        w.execute();
        d.setVisible(true);
    }

    private void showClassStatsDialog(int sectionId, String sectionName) {
        Optional<int[]> weightsOpt = loadWeights();
        int wQ = 20, wM = 30, wE = 50;
        if (weightsOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No saved weights. Use 'Compute Final' first or enter now.");
            String q = JOptionPane.showInputDialog(this, "Quiz %:", "20"); if(q==null) return;
            String m = JOptionPane.showInputDialog(this, "Midterm %:", "30"); if(m==null) return;
            String e = JOptionPane.showInputDialog(this, "EndSem %:", "50"); if(e==null) return;
            try { wQ=Integer.parseInt(q); wM=Integer.parseInt(m); wE=Integer.parseInt(e); } catch(Exception ex){ return; }
        } else {
            wQ = weightsOpt.get()[0]; wM = weightsOpt.get()[1]; wE = weightsOpt.get()[2];
        }
        final int fQ=wQ, fM=wM, fE=wE;

        JDialog d = new JDialog(this, "Stats: " + sectionName, true);
        d.setSize(600, 500); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout());

        SwingWorker<Optional<InstructorService.ClassStats>, Void> w = new SwingWorker<>() {
            @Override protected Optional<InstructorService.ClassStats> doInBackground() {
                return service.computeFinalsForSection(username, sectionId, fQ, fM, fE);
            }
            @Override protected void done() {
                try {
                    var opt = get();
                    if(opt.isPresent()) {
                        var s = opt.get();
                        JPanel p = new JPanel(new BorderLayout());
                        String info = String.format("<html><b>Count:</b> %d | <b>Avg:</b> %.2f | <b>Max:</b> %.2f | <b>Min:</b> %.2f</html>", s.n, s.avg, s.max, s.min);
                        JLabel lbl = new JLabel(info, SwingConstants.CENTER);
                        lbl.setBorder(new EmptyBorder(10,10,10,10));
                        p.add(lbl, BorderLayout.NORTH);
                        // --- USE CUSTOM CHART ---
                        p.add(new SimpleBarChart(s.distribution), BorderLayout.CENTER);
                        d.add(p);
                        d.revalidate();
                    } else { d.add(new JLabel("No data found", SwingConstants.CENTER)); }
                } catch(Exception e) { e.printStackTrace(); }
            }
        };
        w.execute();
        d.setVisible(true);
    }

    // --- CHART COMPONENT (Custom) ---
    private static class SimpleBarChart extends JPanel {
        private final Map<String, Integer> data;
        public SimpleBarChart(Map<String, Integer> data) {
            this.data = data; setBackground(Color.WHITE);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            int maxVal = data.values().stream().max(Integer::compare).orElse(1);
            int barWidth = (w - 100) / Math.max(1, data.size());
            int x = 50;

            g2.setColor(Color.BLACK);
            g2.drawLine(40, h-40, w-20, h-40); // X axis
            g2.drawLine(40, h-40, 40, 20);     // Y axis

            for(var entry : data.entrySet()) {
                int barHeight = (int) ((double) entry.getValue() / maxVal * (h - 80));
                // Random color for fun, or fixed blue
                g2.setColor(new Color(100, 149, 237));
                g2.fillRect(x, h - 40 - barHeight, barWidth - 20, barHeight);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, h - 40 - barHeight, barWidth - 20, barHeight);
                g2.drawString(entry.getKey(), x + (barWidth-20)/2 - 5, h - 20); // Label
                g2.drawString(String.valueOf(entry.getValue()), x + (barWidth-20)/2 - 5, h - 45 - barHeight); // Value
                x += barWidth;
            }
        }
    }

    // --- CUSTOM CARD COMPONENT ---
    private static class DashboardCard extends JPanel {
        private final Color bgColor, textColor;
        private final Runnable onClick;

        DashboardCard(String title, String icon, Color bg, Color text, Runnable onClick) {
            this.bgColor = bg; this.textColor = text; this.onClick = onClick;
            setLayout(new BorderLayout()); setOpaque(false);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
            lblTitle.setForeground(textColor);

            JLabel lblIcon = new JLabel(icon);
            lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
            lblIcon.setForeground(textColor);

            add(lblTitle, BorderLayout.NORTH);
            add(lblIcon, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { if (onClick != null) onClick.run(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            super.paintComponent(g);
        }
    }
}