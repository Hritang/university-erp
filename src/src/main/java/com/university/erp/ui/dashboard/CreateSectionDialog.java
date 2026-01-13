package com.university.erp.ui.dashboard;

import com.university.erp.admin.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CreateSectionDialog extends JDialog {

    private final JTextField courseIdField = new JTextField(5);
    private final JTextField dayField = new JTextField(20);
    private final JTextField roomField = new JTextField(10);
    private final JTextField capField = new JTextField(4);
    private final JTextField semField = new JTextField(10);
    private final JTextField yearField = new JTextField(6);

    private final JButton createBtn = new JButton("Create");
    private final JButton cancelBtn = new JButton("Cancel");

    private final Runnable onSuccess;
    private final AdminService adminService;

    public CreateSectionDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Create Section", true);
        this.onSuccess = onSuccess;
        this.adminService = new AdminService();

        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        p.add(new JLabel("Course ID:"));
        p.add(courseIdField);

        p.add(new JLabel("Day/Time (e.g., Mon 10-12):"));
        p.add(dayField);

        p.add(new JLabel("Room:"));
        p.add(roomField);

        p.add(new JLabel("Capacity:"));
        p.add(capField);

        p.add(new JLabel("Semester:"));
        p.add(semField);

        p.add(new JLabel("Year:"));
        p.add(yearField);

        add(p, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());
        createBtn.addActionListener(e -> onCreate());
    }

    private void onCreate() {
        try {
            int cid = Integer.parseInt(courseIdField.getText().trim());
            int cap = Integer.parseInt(capField.getText().trim());
            int y = Integer.parseInt(yearField.getText().trim());
            String day = dayField.getText().trim();
            String room = roomField.getText().trim();
            String sem = semField.getText().trim();

            if (day.isEmpty() || room.isEmpty() || sem.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            createBtn.setEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return adminService.addSection(cid, null, day, room, cap, sem, y);
                }

                @Override
                protected void done() {
                    createBtn.setEnabled(true);
                    try {
                        boolean success = get(); // This gets the boolean result
                        if (success) {
                            JOptionPane.showMessageDialog(CreateSectionDialog.this, "Section created successfully!");
                            if (onSuccess != null) onSuccess.run();
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(CreateSectionDialog.this, "Failed to create section.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(CreateSectionDialog.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Course ID, Capacity, and Year must be numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}