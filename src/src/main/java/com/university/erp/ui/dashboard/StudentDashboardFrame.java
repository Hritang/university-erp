package com.university.erp.ui.dashboard;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.university.erp.student.StudentService;
import com.university.erp.student.StudentDAO;
import com.university.erp.ui.LoginFrame;
import com.university.erp.ui.dashboard.student.StudentGradesPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class StudentDashboardFrame extends JFrame {

    // --- UI COLORS & THEME ---
    private static final Color BG_DARK_NAVY = new Color(35, 35, 60);
    private static final Color SIDEBAR_BG = new Color(45, 45, 75);
    private static final Color CARD_PURPLE = new Color(120, 130, 235);
    private static final Color CARD_WHITE = new Color(240, 240, 245);
    private static final Color TEXT_ON_PURPLE = Color.WHITE;
    private static final Color TEXT_ON_WHITE = new Color(40, 40, 60);

    private final StudentService studentService = new StudentService();
    private final String username;

    // Tables
    private final JTable catalogTable = new JTable();
    private final JTable registeredTable = new JTable();
    private final JTable timetableTable = new JTable();
    private final JTable sectionTable = new JTable();

    // Layouts
    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerPanel;

    // Maintenance Banner Components
    private final JPanel bannerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JLabel bannerLabel = new JLabel();

    public StudentDashboardFrame(String username) {
        super("University ERP – Student Portal");
        this.username = username;
        installLookAndFeel();

        centerPanel = new JPanel(centerLayout);
        centerPanel.setBackground(BG_DARK_NAVY);

        initUI();

        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            System.err.println("FlatLaf not found.");
        }
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout());

        // --- 0. MAINTENANCE BANNER SETUP ---
        bannerPanel.setBackground(new Color(220, 53, 69)); // Red color
        bannerPanel.setVisible(false); // Hidden by default
        bannerLabel.setForeground(Color.WHITE);
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        bannerPanel.add(new JLabel("⚠ SYSTEM MAINTENANCE: "));
        bannerPanel.add(bannerLabel);

        // Add banner to the very top (NORTH of Frame)
        getContentPane().add(bannerPanel, BorderLayout.NORTH);

        // --- 1. SIDEBAR ---
        JPanel sideBar = new JPanel();
        sideBar.setBackground(SIDEBAR_BG);
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBorder(new EmptyBorder(25, 20, 25, 20));
        sideBar.setPreferredSize(new Dimension(240, getHeight()));

        JLabel title = new JLabel("Student ERP");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideBar.add(title);
        sideBar.add(Box.createVerticalStrut(50));

        sideBar.add(makeSidebarButton("Dashboard", "🏠", () -> centerLayout.show(centerPanel, "home")));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("Catalog", "📚", () -> {
            centerLayout.show(centerPanel, "catalog");
            refreshCatalog();
        }));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("My Courses", "📝", () -> {
            centerLayout.show(centerPanel, "registered");
            refreshRegistered();
        }));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("Timetable", "📅", () -> {
            centerLayout.show(centerPanel, "timetable");
            refreshTimetable();
        }));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("Grades & Transcript", "🎓", () -> {
            centerLayout.show(centerPanel, "grades");
        }));

        sideBar.add(Box.createVerticalGlue());

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
        sideBar.add(logoutBtn);

        // --- 2. CENTER PANELS ---
        centerPanel.add(buildHomePanel(), "home");
        centerPanel.add(buildCatalogPanel(), "catalog");
        centerPanel.add(buildRegisteredPanel(), "registered");
        centerPanel.add(buildTimetablePanel(), "timetable");
        centerPanel.add(new StudentGradesPanel(username), "grades");

        // Layout Assembly
        getContentPane().add(sideBar, BorderLayout.WEST);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        centerLayout.show(centerPanel, "home");

        // --- 3. CHECK MAINTENANCE STATUS ---
        checkMaintenance();
    }

    // --- MAINTENANCE CHECKER METHOD ---
    private void checkMaintenance() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                // Calls the method added to StudentService in Step 1
                return studentService.getMaintenanceBanner();
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
        btn.setMaximumSize(new Dimension(220, 50));
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

    // ===================== HOME DASHBOARD ==========================
    private JPanel buildHomePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 32));
        welcome.setForeground(Color.WHITE);
        wrapper.add(welcome, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 30, 30));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(30, 0, 0, 0));

        grid.add(new DashboardCard("Browse Catalog", "📚", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            centerLayout.show(centerPanel, "catalog"); refreshCatalog();
        }));
        grid.add(new DashboardCard("Register / Drop", "📝", CARD_WHITE, TEXT_ON_WHITE, () -> {
            centerLayout.show(centerPanel, "registered"); refreshRegistered();
        }));
        grid.add(new DashboardCard("My Timetable", "📅", CARD_WHITE, TEXT_ON_WHITE, () -> {
            centerLayout.show(centerPanel, "timetable"); refreshTimetable();
        }));
        grid.add(new DashboardCard("My Grades & Transcript", "🎓", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            centerLayout.show(centerPanel, "grades");
        }));

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    // ===================== CATALOG PANEL ==========================
    private JPanel buildCatalogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Course Catalog");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        // ---- Search bar ----
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        topBar.add(searchPanel, BorderLayout.EAST);
        p.add(topBar, BorderLayout.NORTH);

        // ---- Catalog table ----
        DefaultTableModel catalogModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Code", "Title", "Credits", "Instructor", "Total Seats"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        catalogTable.setModel(catalogModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(catalogModel);
        catalogTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText();
                if (text == null || text.trim().isEmpty()) { sorter.setRowFilter(null); return; }
                try { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim())); } catch (PatternSyntaxException ex) {}
            }
            @Override public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });

        JScrollPane catalogScroll = new JScrollPane(catalogTable);
        p.add(catalogScroll, BorderLayout.CENTER);

        catalogTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = catalogTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = catalogTable.convertRowIndexToModel(row);
                    int courseId = (int) catalogModel.getValueAt(modelRow, 0);
                    loadSections(courseId);
                }
            }
        });

        // ---- Sections table + register button ----
        sectionTable.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"Section ID", "Instructor", "Day/Time", "Room", "Capacity"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) onRegister(); }
        });

        JScrollPane sectionScroll = new JScrollPane(sectionTable);
        sectionScroll.setPreferredSize(new Dimension(960, 180));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(sectionScroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton registerBtn = new JButton("Register Selected");
        registerBtn.addActionListener(e -> onRegister());
        btnPanel.add(registerBtn);
        bottom.add(btnPanel, BorderLayout.SOUTH);

        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ===================== REGISTERED COURSES PANEL ==========================
    private JPanel buildRegisteredPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Registered Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        p.add(title, BorderLayout.NORTH);

        registeredTable.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"Section ID", "Course", "Instructor"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        p.add(new JScrollPane(registeredTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton dropBtn = new JButton("Drop Selected");
        dropBtn.addActionListener(e -> onDrop());
        btnPanel.add(dropBtn);
        p.add(btnPanel, BorderLayout.SOUTH);

        return p;
    }

    // ===================== TIMETABLE PANEL ==========================
    private JPanel buildTimetablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Weekly Timetable");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        p.add(title, BorderLayout.NORTH);

        timetableTable.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"Course", "Day/Time", "Room"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        p.add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        return p;
    }

    // ===================== LOGIC ACTIONS ==========================

    private void onRegister() {
        int row = sectionTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a section to register.");
            return;
        }
        int sectionId = (int) sectionTable.getValueAt(row, 0);

        new SwingWorker<StudentService.RegisterResult, Void>() {
            @Override protected StudentService.RegisterResult doInBackground() {
                return studentService.registerCourseInSection(username, sectionId);
            }
            @Override protected void done() {
                try {
                    StudentService.RegisterResult res = get();
                    if (res.success) {
                        JOptionPane.showMessageDialog(StudentDashboardFrame.this, res.message);
                        refreshRegistered();
                        refreshTimetable();
                    } else {
                        JOptionPane.showMessageDialog(StudentDashboardFrame.this,
                                res.message, "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void onDrop() {
        int row = registeredTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to drop.");
            return;
        }
        int sectionId = (int) registeredTable.getValueAt(row, 0);

        // CHANGED: SwingWorker now expects <String, Void>, not <Boolean, Void>
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                // This now returns a String message (e.g., "SUCCESS" or "Maintenance is ON")
                return studentService.dropCourse(username, sectionId);
            }

            @Override
            protected void done() {
                try {
                    String result = get();

                    if ("SUCCESS".equals(result)) {
                        JOptionPane.showMessageDialog(StudentDashboardFrame.this, "Course dropped successfully.");
                        refreshRegistered();
                        refreshTimetable();
                    } else {
                        // Now we can show the REAL reason (Maintenance, Deadline, etc.)
                        JOptionPane.showMessageDialog(StudentDashboardFrame.this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // ===================== DATA REFRESHERS ==========================

    private void refreshCatalog() {
        new SwingWorker<List<StudentDAO.CourseView>, Void>() {
            @Override protected List<StudentDAO.CourseView> doInBackground() { return studentService.viewCatalog(); }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) catalogTable.getModel();
                    m.setRowCount(0);
                    for (var c : get()) m.addRow(new Object[]{c.id, c.code, c.title, c.credits, c.instructor, c.totalCapacity});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadSections(int courseId) {
        new SwingWorker<List<StudentDAO.SectionInfo>, Void>() {
            @Override protected List<StudentDAO.SectionInfo> doInBackground() { return studentService.getSections(courseId); }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) sectionTable.getModel();
                    m.setRowCount(0);
                    for (var s : get()) m.addRow(new Object[]{s.sectionId, s.instructor, s.dayTime, s.room, s.capacity});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void refreshRegistered() {
        new SwingWorker<List<StudentDAO.RegView>, Void>() {
            @Override protected List<StudentDAO.RegView> doInBackground() { return studentService.viewRegistered(username); }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) registeredTable.getModel();
                    m.setRowCount(0);
                    for (var r : get()) m.addRow(new Object[]{r.sectionId, r.courseCode, r.instructor});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void refreshTimetable() {
        new SwingWorker<List<StudentDAO.TimeView>, Void>() {
            @Override protected List<StudentDAO.TimeView> doInBackground() { return studentService.viewTimetable(username); }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) timetableTable.getModel();
                    m.setRowCount(0);
                    for (var t : get()) m.addRow(new Object[]{t.course, t.dayTime, t.room});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ===================== DASHBOARD CARD ==========================

    private static class DashboardCard extends JPanel {
        private final Color bgColor, textColor;
        private final Runnable onClick;

        DashboardCard(String title, String icon, Color bg, Color text, Runnable onClick) {
            this.bgColor = bg; this.textColor = text; this.onClick = onClick;

            setLayout(new BorderLayout());
            setOpaque(false);
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            super.paintComponent(g);
        }
    }
}
