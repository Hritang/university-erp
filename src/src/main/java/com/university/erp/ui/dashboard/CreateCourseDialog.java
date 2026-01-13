package com.university.erp.ui.dashboard;

import com.university.erp.admin.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CreateCourseDialog extends JDialog {

    private final JTextField codeField = new JTextField(12);
    private final JTextField titleField = new JTextField(20);
    private final JTextField creditsField = new JTextField(4);

    private final JButton createBtn = new JButton("Create");
    private final JButton cancelBtn = new JButton("Cancel");

    private final Runnable onSuccess;
    private final AdminService adminService; // Service to talk to DB

    public CreateCourseDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Create Course", true);
        this.onSuccess = onSuccess;
        this.adminService = new AdminService(); // Create instance of service

        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        p.add(new JLabel("Code (e.g. CS101):"));
        p.add(codeField);

        p.add(new JLabel("Course Title:"));
        p.add(titleField);

        p.add(new JLabel("Credits:"));
        p.add(creditsField);

        add(p, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);


        cancelBtn.addActionListener(e -> dispose());
        createBtn.addActionListener(e -> onCreate());
    }

    private void onCreate() {
        String code = codeField.getText().trim();
        String title = titleField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (code.isEmpty() || title.isEmpty() || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits must be a number.");
            return;
        }

        createBtn.setEnabled(false);


        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // adminService.createCourse returns boolean now
                return adminService.createCourse(code, title, credits);
            }

            @Override
            protected void done() {
                createBtn.setEnabled(true);
                try {
                    boolean success = get();

                    if (success) {
                        JOptionPane.showMessageDialog(CreateCourseDialog.this, "Course created successfully!");
                        if (onSuccess != null) onSuccess.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(CreateCourseDialog.this, "Failed to create course.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CreateCourseDialog.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}