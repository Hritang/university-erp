package com.university.erp.ui.dashboard;


import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.university.erp.admin.*;
import com.university.erp.domain.UserRecord;
import com.university.erp.ui.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class AdminDashboardFrame extends JFrame {

    private static final Color BG_DARK_NAVY = new Color(35, 35, 60);
    private static final Color SIDEBAR_BG = new Color(45, 45, 75);
    private static final Color CARD_PURPLE = new Color(120, 130, 235);
    private static final Color CARD_WHITE = new Color(240, 240, 245);
    private static final Color TEXT_ON_PURPLE = Color.WHITE;
    private static final Color TEXT_ON_WHITE = new Color(40, 40, 60);

    private final AdminService adminService = new AdminService();
    private final AdminDAO adminDAO = new AdminDAO();

    private final JTable coursesTable = new JTable();
    private final JTable sectionsTable = new JTable();
    private final JTable studentsTable = new JTable();
    private final JTable instructorsTable = new JTable();

    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerPanel;

    private DashboardCard maintenanceCard;

    public AdminDashboardFrame(String adminUsername) {
        super("University ERP – Admin Dashboard");
        installLookAndFeel();

        centerPanel = new JPanel(centerLayout);
        centerPanel.setBackground(BG_DARK_NAVY);

        initUI(adminUsername);
        refreshAllTables();

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
            System.err.println("FlatLaf not found. UI may look different.");
        }
    }

    private void initUI(String adminUsername) {
        getContentPane().setLayout(new BorderLayout());

        JPanel sideBar = new JPanel();
        sideBar.setBackground(SIDEBAR_BG);
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBorder(new EmptyBorder(25, 20, 25, 20));
        sideBar.setPreferredSize(new Dimension(240, getHeight()));

        JLabel title = new JLabel("Admin ERP");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideBar.add(title);
        sideBar.add(Box.createVerticalStrut(50));

        sideBar.add(makeSidebarButton("Dashboard", "🏠", () -> centerLayout.show(centerPanel, "home")));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("Users", "👥", () -> {
            centerLayout.show(centerPanel, "users");
            refreshUsersTables();
        }));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("Courses", "📚", () -> {
            centerLayout.show(centerPanel, "courses");
            refreshCoursesTable();
        }));
        sideBar.add(Box.createVerticalStrut(10));

        sideBar.add(makeSidebarButton("Sections", "📅", () -> {
            centerLayout.show(centerPanel, "sections");
            refreshSectionsTable();
        }));

        sideBar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        sideBar.add(logoutBtn);

        centerPanel.add(buildHomePanel(adminUsername), "home");
        centerPanel.add(buildUsersPanel(), "users");
        centerPanel.add(buildCoursesPanel(), "courses");
        centerPanel.add(buildSectionsPanel(), "sections");

        centerLayout.show(centerPanel, "home");

        getContentPane().add(sideBar, BorderLayout.WEST);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
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

    private JPanel buildHomePanel(String username) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome back, " + username);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 32));
        welcome.setForeground(Color.WHITE);
        wrapper.add(welcome, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3, 20, 20));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(30, 0, 0, 0));

        grid.add(new DashboardCard("Manage Users", "👥", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            centerLayout.show(centerPanel, "users"); refreshUsersTables();
        }));
        grid.add(new DashboardCard("Courses", "📚", CARD_WHITE, TEXT_ON_WHITE, () -> {
            centerLayout.show(centerPanel, "courses"); refreshCoursesTable();
        }));
        grid.add(new DashboardCard("Sections", "📅", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            centerLayout.show(centerPanel, "sections"); refreshSectionsTable();
        }));

        grid.add(new DashboardCard("Assign Instr.", "🔁", CARD_WHITE, TEXT_ON_WHITE, () -> {
            AssignInstructorDialog d = new AssignInstructorDialog(this);
            d.setVisible(true);
            refreshSectionsTable();
        }));

        maintenanceCard = new DashboardCard("Maintenance", "⚙", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            ToggleMaintenanceDialog d = new ToggleMaintenanceDialog(this);
            d.setVisible(true);
            updateMaintenanceStatusUI();
        });
        grid.add(maintenanceCard);

        grid.add(new DashboardCard("Backup DB", "💾", CARD_WHITE, TEXT_ON_WHITE, () -> {
            onBackupClicked();
        }));

        grid.add(new DashboardCard("Restore DB", "♻", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            onRestoreClicked();
        }));

        grid.add(new DashboardCard("Reg Deadline", "⏰", CARD_WHITE, TEXT_ON_WHITE, () -> {
            showDeadlineDialog("registration_deadline", "Registration Deadline");
        }));

        grid.add(new DashboardCard("Drop Deadline", "📌", CARD_PURPLE, TEXT_ON_PURPLE, () -> {
            showDeadlineDialog("drop_deadline", "Course Drop Deadline");
        }));

        wrapper.add(grid, BorderLayout.CENTER);
        updateMaintenanceStatusUI();

        return wrapper;
    }

    private void updateMaintenanceStatusUI() {
        if (maintenanceCard == null) return;
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() { return adminService.isMaintenanceOn(); }
            @Override protected void done() {
                try {
                    boolean isOn = get();
                    if (isOn) maintenanceCard.setStatusText("⚠ MODE: ON", new Color(255, 80, 80));
                    else maintenanceCard.setStatusText("● Mode: Normal", new Color(150, 255, 150));
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        w.execute();
    }

    private JPanel buildUsersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        wrapper.add(title, BorderLayout.NORTH);

        studentsTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Username","Status","Roll","Program","Year"}));
        instructorsTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Username","Status","Department"}));

        JScrollPane sp1 = new JScrollPane(studentsTable); sp1.setBorder(BorderFactory.createTitledBorder("Students"));
        JScrollPane sp2 = new JScrollPane(instructorsTable); sp2.setBorder(BorderFactory.createTitledBorder("Instructors"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp1, sp2);
        split.setResizeWeight(0.5);
        wrapper.add(split, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton addBtn = new JButton("Add User");
        addBtn.addActionListener(e -> { AddUserDialog d = new AddUserDialog(this); d.setVisible(true); refreshUsersTables(); });
        btnPanel.add(addBtn);

        JButton deleteUserBtn = new JButton("Delete User");
        deleteUserBtn.addActionListener(e -> onDeleteUser());
        btnPanel.add(deleteUserBtn);

        wrapper.add(btnPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildCoursesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Course Catalog");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        wrapper.add(title, BorderLayout.NORTH);

        coursesTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Code","Title","Credits"}));
        wrapper.add(new JScrollPane(coursesTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton create = new JButton("Create");
        create.addActionListener(e -> { CreateCourseDialog d = new CreateCourseDialog(this, this::refreshCoursesTable); d.setVisible(true); });
        JButton edit = new JButton("Edit Selected");
        edit.addActionListener(e -> onEditCourse());
        JButton delete = new JButton("Delete");
        delete.setForeground(Color.RED);
        delete.addActionListener(e -> onDeleteCourse());

        btns.add(create); btns.add(edit); btns.add(delete);
        wrapper.add(btns, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildSectionsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Active Sections");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        wrapper.add(title, BorderLayout.NORTH);

        sectionsTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"ID","Course","Instructor","Time","Room","Cap","Sem","Year"}));
        wrapper.add(new JScrollPane(sectionsTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton create = new JButton("Create");
        create.addActionListener(e -> { CreateSectionDialog d = new CreateSectionDialog(this, this::refreshSectionsTable); d.setVisible(true); });
        JButton edit = new JButton("Edit Selected");
        edit.addActionListener(e -> onEditSection());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> onDeleteSection());

        btns.add(create); btns.add(edit); btns.add(delete);
        wrapper.add(btns, BorderLayout.SOUTH);
        return wrapper;
    }

    // --- LOGIC ---
    private void refreshAllTables() { refreshCoursesTable(); refreshSectionsTable(); refreshUsersTables(); }

    private void refreshCoursesTable() {
        SwingWorker<List<Course>, Void> w = new SwingWorker<>() {
            @Override protected List<Course> doInBackground() { return adminService.listCourses(); }
            @Override protected void done() {
                try {
                    List<Course> list = get();
                    DefaultTableModel m = (DefaultTableModel) coursesTable.getModel(); m.setRowCount(0);
                    for (Course c : list) m.addRow(new Object[]{c.getCourseId(), c.getCode(), c.getTitle(), c.getCredits()});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }; w.execute();
    }

    private void refreshSectionsTable() {
        SwingWorker<List<Section>, Void> w = new SwingWorker<>() {
            @Override protected List<Section> doInBackground() { return adminService.listSections(); }
            @Override protected void done() {
                try {
                    List<Section> list = get();
                    DefaultTableModel m = (DefaultTableModel) sectionsTable.getModel(); m.setRowCount(0);
                    for (Section s : list) {
                        String instr = s.getInstructorUsername() == null ? "Unassigned" : s.getInstructorUsername();
                        m.addRow(new Object[]{s.getSectionId(), s.getCourseCode(), instr, s.getDayTime(), s.getRoom(), s.getCapacity(), s.getSemester(), s.getYear()});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }; w.execute();
    }

    private void refreshUsersTables() {
        SwingWorker<List<AdminDAO.StudentDetail>, Void> sw1 = new SwingWorker<>() {
            @Override protected List<AdminDAO.StudentDetail> doInBackground() { return adminService.listStudentDetails(); }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) studentsTable.getModel(); m.setRowCount(0);
                    for (var s : get()) m.addRow(new Object[]{s.getUserId(), s.getUsername(), s.getStatus(), s.getRollNo(), s.getProgram(), s.getYear()});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }; sw1.execute();
        SwingWorker<List<AdminDAO.InstructorDetail>, Void> sw2 = new SwingWorker<>() {
            @Override protected List<AdminDAO.InstructorDetail> doInBackground() { return adminService.listInstructorDetails(); }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) instructorsTable.getModel(); m.setRowCount(0);
                    for (var i : get()) m.addRow(new Object[]{i.getUserId(), i.getUsername(), i.getStatus(), i.getDepartment()});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }; sw2.execute();
    }

    private void onEditCourse() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a course."); return; }
        int id = (int) coursesTable.getValueAt(row, 0);
        try {
            adminDAO.findCourseById(id).ifPresentOrElse(c -> {
                EditCourseDialog d = new EditCourseDialog(this, c); d.setVisible(true); refreshCoursesTable();
            }, () -> JOptionPane.showMessageDialog(this, "Course not found"));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage()); }
    }

    private void onEditSection() {
        int row = sectionsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        int id = (int) sectionsTable.getValueAt(row, 0);
        try {
            adminDAO.findSectionById(id).ifPresentOrElse(s -> {
                EditSectionDialog d = new EditSectionDialog(this, s); d.setVisible(true); refreshSectionsTable();
            }, () -> JOptionPane.showMessageDialog(this, "Section not found"));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage()); }
    }

    private void onBackupClicked() {
        if (JOptionPane.showConfirmDialog(this, "Backup DB?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            SwingWorker<String, Void> w = new SwingWorker<>() {
                @Override protected String doInBackground() {
                    try { return adminDAO.backupERP(null); } catch (Exception ex) { return "Error: " + ex.getMessage(); }
                }
                @Override protected void done() {
                    try { JOptionPane.showMessageDialog(AdminDashboardFrame.this, get()); } catch (Exception e) { e.printStackTrace(); }
                }
            }; w.execute();
        }
    }

    private void onRestoreClicked() {
        String tag = JOptionPane.showInputDialog(this, "Enter Backup Tag (e.g., 20241119_215040):", "Restore DB", JOptionPane.PLAIN_MESSAGE);
        if (tag == null || tag.isBlank()) return;
        if (JOptionPane.showConfirmDialog(this, "⚠ WARNING!\nRestoring will OVERWRITE current DB!\nProceed?", "Confirm Restore", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() {
                try { return adminDAO.restoreERP(tag) ? "Restored!" : "Failed!"; } catch (Exception ex) { return "Error: " + ex.getMessage(); }
            }
            @Override protected void done() { try { JOptionPane.showMessageDialog(AdminDashboardFrame.this, get()); } catch (Exception e) {} }
        }; w.execute();
    }

    private void onDeleteCourse() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a course."); return; }
        int courseId = (int) coursesTable.getValueAt(row, 0);

        new SwingWorker<Boolean, Void>() {
            List<Section> sections;
            @Override protected Boolean doInBackground() {
                try { return adminService.canDeleteCourse(courseId); } catch (Exception ex) { return false; }
            }
            @Override protected void done() {
                try {
                    boolean allowed = get();
                    if (!allowed) {
                        JOptionPane.showMessageDialog(AdminDashboardFrame.this, "Cannot delete: students enrolled in sections.", "Blocked", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // Check for sections to cascade delete
                    sections = adminService.getSectionsByCourse(courseId);
                    if (!sections.isEmpty()) {
                        int choice = JOptionPane.showConfirmDialog(AdminDashboardFrame.this, "Delete course and all its " + sections.size() + " sections?", "Cascade Delete", JOptionPane.YES_NO_OPTION);
                        if (choice != JOptionPane.YES_OPTION) return;
                        for (Section s : sections) adminService.deleteSectionForced(s.getSectionId());
                    }
                    adminService.deleteCourse(courseId);
                    JOptionPane.showMessageDialog(AdminDashboardFrame.this, "Course deleted.");
                    refreshCoursesTable();
                    refreshSectionsTable();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void onDeleteSection() {
        int row = sectionsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        int sectionId = (int) sectionsTable.getValueAt(row, 0);
        if (!adminService.canDeleteSection(sectionId)) {
            JOptionPane.showMessageDialog(this, "Cannot delete: students enrolled.", "Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }
        adminService.deleteSection(sectionId);
        JOptionPane.showMessageDialog(this, "Section deleted.");
        refreshSectionsTable();
    }

    private void onDeleteUser() {
        int rowS = studentsTable.getSelectedRow();
        int rowI = instructorsTable.getSelectedRow();
        int userId = -1;
        if (rowS >= 0) userId = (int) studentsTable.getValueAt(rowS, 0);
        else if (rowI >= 0) userId = (int) instructorsTable.getValueAt(rowI, 0);
        else { JOptionPane.showMessageDialog(this, "Select a user."); return; }

        if (userId == adminService.getLoggedInAdminId()) {
            JOptionPane.showMessageDialog(this, "Cannot delete yourself.", "Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }
        adminService.deleteUser(userId);
        JOptionPane.showMessageDialog(this, "User deleted.");
        refreshUsersTables();
    }

    private void showDeadlineDialog(String settingKey, String title) {
        String existing = "";
        try { existing = adminDAO.getSetting(settingKey).orElse(""); } catch (Exception ignored) {}

        String msg = "Enter date (YYYY-MM-DD)";
        String date = JOptionPane.showInputDialog(this, msg + "\nCurrent: " + existing, title, JOptionPane.PLAIN_MESSAGE);

        if (date == null || date.isBlank()) return;
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid Format! Use YYYY-MM-DD", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            adminDAO.setSetting(settingKey, date);
            JOptionPane.showMessageDialog(this, title + " Updated: " + date);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }


    private static class DashboardCard extends JPanel {
        private final Color bgColor, textColor;
        private final Runnable onClick;
        private final JLabel statusLabel = new JLabel(" ");

        DashboardCard(String title, String icon, Color bg, Color text, Runnable onClick) {
            this.bgColor = bg; this.textColor = text; this.onClick = onClick;
            setLayout(new BorderLayout()); setOpaque(false);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20)); lblTitle.setForeground(textColor);
            JLabel lblIcon = new JLabel(icon); lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 48)); lblIcon.setForeground(textColor);
            statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            add(lblTitle, BorderLayout.NORTH); add(lblIcon, BorderLayout.CENTER); add(statusLabel, BorderLayout.SOUTH);
            addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { if(onClick != null) onClick.run(); }});
        }
        public void setStatusText(String text, Color color) { statusLabel.setText(text); statusLabel.setForeground(color); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            super.paintComponent(g);
        }
    }

    private class AddUserDialog extends JDialog {
        private final JTextField usernameField = new JTextField(20);
        private final JPasswordField passwordField = new JPasswordField(20);
        private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"student", "instructor", "admin"});
        private final JTextField rollField = new JTextField(12), programField = new JTextField(12), yearField = new JTextField(4), deptField = new JTextField(20);
        private final JLabel rollLabel = new JLabel("Roll No:"), progLabel = new JLabel("Program:"), yearLabel = new JLabel("Year:"), deptLabel = new JLabel("Department:");
        private final JButton createBtn = new JButton("Create");

        AddUserDialog(Frame f) {
            super(f, "Add User", true);
            JPanel p = new JPanel(new GridBagLayout()); p.setBorder(new EmptyBorder(20,20,20,20));
            GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5,5,5,5); c.fill = GridBagConstraints.HORIZONTAL; c.gridx=0; c.gridy=0;

            p.add(new JLabel("Username:"), c); c.gridx=1; p.add(usernameField, c);
            c.gridx=0; c.gridy++; p.add(new JLabel("Password:"), c); c.gridx=1; p.add(passwordField, c);
            c.gridx=0; c.gridy++; p.add(new JLabel("Role:"), c); c.gridx=1; p.add(roleCombo, c);

            c.gridx=0; c.gridy++; p.add(rollLabel, c); c.gridx=1; p.add(rollField, c);
            c.gridx=0; c.gridy++; p.add(progLabel, c); c.gridx=1; p.add(programField, c);
            c.gridx=0; c.gridy++; p.add(yearLabel, c); c.gridx=1; p.add(yearField, c);
            c.gridx=0; c.gridy++; p.add(deptLabel, c); c.gridx=1; p.add(deptField, c);

            add(p, BorderLayout.CENTER);
            JPanel b = new JPanel(); b.add(createBtn); JButton can = new JButton("Cancel"); can.addActionListener(e->dispose()); b.add(can);
            add(b, BorderLayout.SOUTH);

            roleCombo.addActionListener(e -> updateFields());
            createBtn.addActionListener(e -> onCreate());
            updateFields(); pack(); setLocationRelativeTo(f);
        }

        private void updateFields() {
            String r = (String) roleCombo.getSelectedItem();
            boolean s = "student".equals(r), i = "instructor".equals(r);
            rollLabel.setVisible(s); rollField.setVisible(s); progLabel.setVisible(s); programField.setVisible(s); yearLabel.setVisible(s); yearField.setVisible(s);
            deptLabel.setVisible(i); deptField.setVisible(i);
            pack();
        }

        private void onCreate() {
            String u = usernameField.getText().trim(), pw = new String(passwordField.getPassword()), r = (String) roleCombo.getSelectedItem();
            if(u.isEmpty() || pw.isEmpty()) { JOptionPane.showMessageDialog(this, "Username/Pass required", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }

            String rl = rollField.getText(), pr = programField.getText(), yrStr = yearField.getText(), dp = deptField.getText();
            Integer yr = null;
            if("student".equals(r)) {
                if(rl.isEmpty() || pr.isEmpty() || yrStr.isEmpty()) { JOptionPane.showMessageDialog(this, "All Student fields required", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }
                try {
                    yr = Integer.parseInt(yrStr);
                    if (yr < 2000 || yr > 2100) { JOptionPane.showMessageDialog(this, "Invalid Year (must be > 2000)", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }
                } catch(Exception e) { JOptionPane.showMessageDialog(this, "Year must be a number", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }
            }
            if("instructor".equals(r) && dp.isEmpty()) { JOptionPane.showMessageDialog(this, "Department required", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }

            createBtn.setEnabled(false);
            Integer finalYr = yr;

            SwingWorker<String, Void> w = new SwingWorker<>() {
                @Override protected String doInBackground() {
                    try {
                        adminService.addUser(u, r, pw, "active", rl, pr, finalYr, dp);
                        return null;
                    } catch (Exception ex) { return ex.getMessage(); }
                }
                @Override protected void done() {
                    createBtn.setEnabled(true);
                    try {
                        String err = get();
                        if (err == null) {
                            JOptionPane.showMessageDialog(AddUserDialog.this, "User Created Successfully!");
                            dispose(); refreshUsersTables();
                        } else {

                            JOptionPane.showMessageDialog(AddUserDialog.this, "Failed to create user:\n" + err, "Database Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            };
            w.execute();
        }
    }

    private class AssignInstructorDialog extends JDialog {
        private final JComboBox<String> sectionBox = new JComboBox<>();
        private final JComboBox<String> instrBox = new JComboBox<>();
        private final JButton assignBtn = new JButton("Assign");
        private final JButton cancelBtn = new JButton("Cancel");

        private java.util.List<Section> sList;
        private java.util.List<UserRecord> iList;

        AssignInstructorDialog(Frame owner) {
            super(owner, "Assign Instructor", true);

            setSize(500, 300);

            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25)); // Big padding around edges

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10); // Spacing between components
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
            mainPanel.add(new JLabel("Select Section:"), gbc);

            gbc.gridx = 1; gbc.weightx = 0.7;
            sectionBox.setPreferredSize(new Dimension(250, 35)); // Make dropdown wider/taller
            mainPanel.add(sectionBox, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
            mainPanel.add(new JLabel("Select Instructor:"), gbc);

            gbc.gridx = 1; gbc.weightx = 0.7;
            instrBox.setPreferredSize(new Dimension(250, 35));
            mainPanel.add(instrBox, gbc);

            add(mainPanel, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            assignBtn.setPreferredSize(new Dimension(100, 35)); // Bigger button
            cancelBtn.setPreferredSize(new Dimension(100, 35));

            btnPanel.add(assignBtn);
            btnPanel.add(cancelBtn);
            add(btnPanel, BorderLayout.SOUTH);

            loadData();

            assignBtn.addActionListener(e -> onAssign());
            cancelBtn.addActionListener(e -> dispose());

            setLocationRelativeTo(owner);
        }

        private void loadData() {
            sectionBox.addItem("Loading...");
            instrBox.addItem("Loading...");
            assignBtn.setEnabled(false);

            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    sList = adminService.listSections();
                    iList = adminService.listInstructors();
                    return null;
                }

                @Override
                protected void done() {
                    sectionBox.removeAllItems();
                    instrBox.removeAllItems();

                    if (sList != null) {
                        for (Section s : sList) {
                            // Format: "CS101 - Intro to Java (Sec 1)"
                            sectionBox.addItem(s.getCourseCode() + " - " + s.getCourseTitle() + " (ID: " + s.getSectionId() + ")");
                        }
                    }

                    if (iList != null) {
                        for (UserRecord u : iList) {
                            instrBox.addItem(u.getUsername() + " (ID: " + u.getUserId() + ")");
                        }
                    }

                    assignBtn.setEnabled(true);
                }
            };
            w.execute();
        }

        private void onAssign() {
            if (sectionBox.getItemCount() == 0 || instrBox.getItemCount() == 0) return;

            int sIndex = sectionBox.getSelectedIndex();
            int iIndex = instrBox.getSelectedIndex();

            if (sIndex < 0 || iIndex < 0) {
                JOptionPane.showMessageDialog(this, "Please select both a section and an instructor.");
                return;
            }

            int sectionId = sList.get(sIndex).getSectionId();
            int instructorId = iList.get(iIndex).getUserId();

            assignBtn.setEnabled(false);

            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return adminService.assignInstructorToSection(sectionId, instructorId);
                }

                @Override
                protected void done() {
                    assignBtn.setEnabled(true);
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(AssignInstructorDialog.this, "Instructor Assigned Successfully!");
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(AssignInstructorDialog.this, "Failed to assign instructor.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(AssignInstructorDialog.this, "Error: " + e.getMessage());
                    }
                }
            };
            w.execute();
        }
    }


    private class ToggleMaintenanceDialog extends JDialog {

            private final JRadioButton onRadio = new JRadioButton("Maintenance ON");
            private final JRadioButton offRadio = new JRadioButton("Maintenance OFF");
            private final ButtonGroup statusGroup = new ButtonGroup();

            private final JTextField bannerField = new JTextField(25);
            private final JButton applyBtn = new JButton("Apply Changes");
            private final JButton cancelBtn = new JButton("Cancel");

               ToggleMaintenanceDialog(Frame owner) {
                super(owner, "System Maintenance Settings", true);


                setSize(450, 300);
                setLayout(new BorderLayout());

                JPanel mainPanel = new JPanel(new GridBagLayout());
                mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(10, 5, 10, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0;


                gbc.gridx = 0; gbc.gridy = 0;
                mainPanel.add(new JLabel("Set System Status:"), gbc);

                JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));


                onRadio.setForeground(new Color(255, 80, 80)); // Light Red
                onRadio.setFont(onRadio.getFont().deriveFont(Font.BOLD));

                offRadio.setForeground(new Color(100, 255, 100)); // Light Green
                offRadio.setFont(offRadio.getFont().deriveFont(Font.BOLD));

                statusGroup.add(onRadio);
                statusGroup.add(offRadio);

                radioPanel.add(offRadio);
                radioPanel.add(onRadio);

                gbc.gridy = 1;
                mainPanel.add(radioPanel, gbc);


                gbc.gridy = 2;
                JLabel helpLabel = new JLabel("Banner Message (What users see on login):");
                helpLabel.setForeground(Color.GRAY);
                mainPanel.add(helpLabel, gbc);

                gbc.gridy = 3;

                bannerField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5))
                );
                mainPanel.add(bannerField, gbc);

                add(mainPanel, BorderLayout.CENTER);


                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnPanel.add(applyBtn);
                btnPanel.add(cancelBtn);
                add(btnPanel, BorderLayout.SOUTH);


                applyBtn.addActionListener(e -> onApply());
                cancelBtn.addActionListener(e -> dispose());


                offRadio.setSelected(true);

                loadCurrentSettings();
                setLocationRelativeTo(owner);
            }

            private void loadCurrentSettings() {

                applyBtn.setEnabled(false);

                SwingWorker<Void, Void> w = new SwingWorker<>() {
                    boolean isOn = false;
                    String currentBanner = "";

                    @Override
                    protected Void doInBackground() {

                        isOn = adminService.isMaintenanceOn();
                        try {
                            currentBanner = adminDAO.getSetting("maintenance_banner").orElse("");
                        } catch (Exception e) { e.printStackTrace(); }
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (isOn) onRadio.setSelected(true);
                        else offRadio.setSelected(true);

                       bannerField.setText(currentBanner);
                       applyBtn.setEnabled(true);
                    }
                };
                w.execute();
            }

            private void onApply() {
                boolean turnOn = onRadio.isSelected();
                String bannerText = bannerField.getText();

                applyBtn.setEnabled(false);

                SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        return adminService.setMaintenance(turnOn, bannerText);
                    }

                    @Override
                    protected void done() {
                        applyBtn.setEnabled(true);
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(ToggleMaintenanceDialog.this, "Maintenance settings updated successfully.");
                                dispose();

                               updateMaintenanceStatusUI();
                            } else {
                                JOptionPane.showMessageDialog(ToggleMaintenanceDialog.this, "Failed to update settings.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                w.execute();
            }
        }


    private class CreateCourseDialog extends JDialog {
        private final JTextField code = new JTextField(), title = new JTextField(), cred = new JTextField();
        private final Runnable ok;
        CreateCourseDialog(Frame f, Runnable ok) {
            super(f, "Create Course", true); this.ok = ok; setSize(300, 250);
            JPanel p = new JPanel(new GridLayout(3,2)); p.setBorder(new EmptyBorder(10,10,10,10));
            p.add(new JLabel("Code:")); p.add(code);
            p.add(new JLabel("Title:")); p.add(title); p.add(new JLabel("Credits:")); p.add(cred);
            add(p, BorderLayout.CENTER); JButton b = new JButton("Create"); b.addActionListener(e->save()); add(b, BorderLayout.SOUTH);
            setLocationRelativeTo(f);
        }
        void save() {
            try {
                int c = Integer.parseInt(cred.getText());
                if (c <= 0) { JOptionPane.showMessageDialog(this, "Credits must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }
                if(adminService.createCourse(code.getText(), title.getText(), c)) { ok.run(); dispose(); } else JOptionPane.showMessageDialog(this, "Failed");
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Credits (Must be number)"); }
        }
    }


    private class EditCourseDialog extends JDialog {
        private final JTextField code = new JTextField(), title = new JTextField(), cred = new JTextField();
        private final Course course;
        EditCourseDialog(Frame f, Course c) {
            super(f, "Edit Course", true); this.course = c; setSize(300, 250);
            JPanel p = new JPanel(new GridLayout(3,2)); p.setBorder(new EmptyBorder(10,10,10,10));
            p.add(new JLabel("Code:")); p.add(code); code.setText(c.getCode());
            p.add(new JLabel("Title:")); p.add(title); title.setText(c.getTitle());
            p.add(new JLabel("Credits:")); p.add(cred); cred.setText(""+c.getCredits());
            add(p, BorderLayout.CENTER); JButton b = new JButton("Save"); b.addActionListener(e->save()); add(b, BorderLayout.SOUTH);
            setLocationRelativeTo(f);
        }
        void save() {
            try {
                int c = Integer.parseInt(cred.getText());
                if (c <= 0) { JOptionPane.showMessageDialog(this, "Credits must be positive"); return; }
                if(adminService.updateCourse(course.getCourseId(), code.getText(), title.getText(), c)) dispose(); else JOptionPane.showMessageDialog(this, "Failed");
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input"); }
        }
    }


    private class CreateSectionDialog extends JDialog {
        private final JTextField cid = new JTextField(), day = new JTextField(), room = new JTextField();
        private final JTextField cap = new JTextField(), sem = new JTextField(), yr = new JTextField();
        private final Runnable ok;
        CreateSectionDialog(Frame f, Runnable ok) {
            super(f, "Create Section", true); this.ok=ok; setSize(300, 350);
            JPanel p = new JPanel(new GridLayout(6,2)); p.setBorder(new EmptyBorder(10,10,10,10));
            p.add(new JLabel("Course ID:")); p.add(cid); p.add(new JLabel("Day/Time:")); p.add(day);
            p.add(new JLabel("Room:")); p.add(room); p.add(new JLabel("Capacity:")); p.add(cap);
            p.add(new JLabel("Sem:")); p.add(sem); p.add(new JLabel("Year:")); p.add(yr);
            add(p, BorderLayout.CENTER); JButton b = new JButton("Create"); b.addActionListener(e->save()); add(b, BorderLayout.SOUTH);
            setLocationRelativeTo(f);
        }
        void save() {
            try {
                int ci = Integer.parseInt(cid.getText());
                int cp = Integer.parseInt(cap.getText());
                int y = Integer.parseInt(yr.getText());
                if (cp <= 0) { JOptionPane.showMessageDialog(this, "Capacity must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }
                if (y < 2024) { JOptionPane.showMessageDialog(this, "Year must be current or future", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }

                if(adminService.addSection(ci, null, day.getText(), room.getText(), cp, sem.getText(), y)) { ok.run(); dispose(); }
                else JOptionPane.showMessageDialog(this, "Failed (Check Course ID)");
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Number Format"); }
        }
    }


    private class EditSectionDialog extends JDialog {
        private final JTextField day = new JTextField(), room = new JTextField(), cap = new JTextField(), sem = new JTextField(), yr = new JTextField();
        private final Section section;
        EditSectionDialog(Frame f, Section s) {
            super(f, "Edit Section", true); this.section=s; setSize(300, 350);
            JPanel p = new JPanel(new GridLayout(5,2)); p.setBorder(new EmptyBorder(10,10,10,10));
            p.add(new JLabel("Day/Time:")); p.add(day); day.setText(s.getDayTime());
            p.add(new JLabel("Room:")); p.add(room); room.setText(s.getRoom());
            p.add(new JLabel("Capacity:")); p.add(cap); cap.setText(""+s.getCapacity());
            p.add(new JLabel("Sem:")); p.add(sem); sem.setText(s.getSemester());
            p.add(new JLabel("Year:")); p.add(yr); yr.setText(""+s.getYear());
            add(p, BorderLayout.CENTER); JButton b = new JButton("Save"); b.addActionListener(e->save()); add(b, BorderLayout.SOUTH);
            setLocationRelativeTo(f);
        }
        void save() {
            try {
                int cp = Integer.parseInt(cap.getText());
                int y = Integer.parseInt(yr.getText());
                if (cp <= 0) { JOptionPane.showMessageDialog(this, "Capacity must be positive", "Validation Error", JOptionPane.WARNING_MESSAGE); return; }

                if(adminService.updateSection(section.getSectionId(), section.getInstructorId(), day.getText(), room.getText(), cp, sem.getText(), y)) dispose();
                else JOptionPane.showMessageDialog(this, "Update Failed");
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Number"); }
        }
    }
}