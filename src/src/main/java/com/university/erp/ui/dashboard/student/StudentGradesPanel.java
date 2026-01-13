package com.university.erp.ui.dashboard.student;

import com.university.erp.student.StudentDAO;
import com.university.erp.student.StudentService;
import com.university.erp.student.TranscriptExporter;        // <-- Correct CSV exporter
import com.university.erp.student.TranscriptPDFExporter;    // <-- Correct PDF exporter

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentGradesPanel extends JPanel {

    private final StudentService service = new StudentService();
    private final JTable table = new JTable();
    private final JButton refreshBtn = new JButton("Refresh");

    public StudentGradesPanel(String username) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ==== PANEL TITLE ====
        JLabel title = new JLabel("My Grades");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        refreshBtn.addActionListener(e -> loadGrades(username));

        // ==== TABLE SETUP ====
        table.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"Code", "Title", "Component", "Score", "Final Grade"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ==== BOTTOM BUTTONS (CSV + PDF) ====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton csvBtn = new JButton("Export Transcript (CSV)");
        csvBtn.addActionListener(e -> exportCSV(username));

        JButton pdfBtn = new JButton("Export Transcript (PDF)");
        pdfBtn.addActionListener(e -> exportPDF(username));

        bottom.add(csvBtn);
        bottom.add(pdfBtn);

        add(bottom, BorderLayout.SOUTH);

        // ==== INITIAL LOAD ====
        loadGrades(username);
    }

    private void loadGrades(String username) {
        List<StudentDAO.GradeView> list = service.viewGrades(username);
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for (StudentDAO.GradeView g : list) {
            m.addRow(new Object[]{g.code, g.title, g.component, g.score, g.finalGrade});
        }
    }

    // ==== EXPORT CSV METHOD ====
    private void exportCSV(String username) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Transcript (CSV)");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean ok = TranscriptExporter.exportCSV(        // <---- Correct class here
                    chooser.getSelectedFile().getAbsolutePath(),
                    service.viewGrades(username)
            );
            JOptionPane.showMessageDialog(this, ok ? "Transcript saved successfully!" : "Error saving transcript!");
        }
    }

    // ==== EXPORT PDF METHOD ====
    private void exportPDF(String username) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Transcript (PDF)");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean ok = TranscriptPDFExporter.exportPDF(
                    chooser.getSelectedFile().getAbsolutePath(),
                    service.viewGrades(username),
                    username
            );
            JOptionPane.showMessageDialog(this, ok ? "PDF saved successfully!" : "Error saving PDF!");
        }
    }
}


