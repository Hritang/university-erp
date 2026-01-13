//package com.university.erp.ui.dashboard.student;
//
//import com.university.erp.student.StudentDAO;
//import com.university.erp.student.StudentService;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.util.List;
//
//public class StudentCatalogPanel extends JPanel {
//
//    private final StudentService service = new StudentService();
//    private final JTable table = new JTable();
//    private final JButton refreshBtn = new JButton("Refresh");
//
//    public StudentCatalogPanel(String username) {
//        setLayout(new BorderLayout());
//        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//
//        JLabel title = new JLabel("📚 Course Catalog");
//        title.setFont(new Font("SansSerif", Font.BOLD, 22));
//
//        JPanel top = new JPanel(new BorderLayout());
//        top.add(title, BorderLayout.WEST);
//        top.add(refreshBtn, BorderLayout.EAST);
//
//        refreshBtn.addActionListener(e -> loadCatalog());
//
//        table.setModel(new DefaultTableModel(new Object[][]{},
//                new String[]{"Code", "Title", "Credits", "Instructor", "Total Seats"}));
//
//        add(top, BorderLayout.NORTH);
//        add(new JScrollPane(table), BorderLayout.CENTER);
//
//        loadCatalog();
//    }
//
//    private void loadCatalog() {
//        List<StudentDAO.CourseView> list = service.viewCatalog();
//        DefaultTableModel m = (DefaultTableModel) table.getModel();
//        m.setRowCount(0);
//        for (StudentDAO.CourseView c : list) {
//            m.addRow(new Object[]{c.code, c.title, c.credits, c.instructor, c.totalCapacity});
//        }
//    }
//}


package com.university.erp.ui.dashboard.student;

import com.university.erp.student.StudentDAO;
import com.university.erp.student.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Catalog panel that includes hidden ID column (so UI actions can read real PK).
 */
public class StudentCatalogPanel extends JPanel {

    private final StudentService service = new StudentService();
    private final JTable table = new JTable();
    private final JButton refreshBtn = new JButton("Refresh");

    public StudentCatalogPanel(String username) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("📚 Course Catalog");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        refreshBtn.addActionListener(e -> loadCatalog());

        // NOTE: include ID as first column (hidden visually)
        table.setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"ID", "Code", "Title", "Credits", "Instructor", "Total Seats"}) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });

        // Hide the ID column visually but keep it in the model
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadCatalog();
    }

    private void loadCatalog() {
        List<StudentDAO.CourseView> list = service.viewCatalog();
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for (StudentDAO.CourseView c : list) {
            m.addRow(new Object[]{c.id, c.code, c.title, c.credits, c.instructor, c.totalCapacity});
        }
    }

    /**
     * Utility for other frames to read selected course id.
     */
    public Integer getSelectedCourseId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object o = table.getValueAt(row, 0);
        if (o instanceof Integer) return (Integer) o;
        try { return Integer.parseInt(o.toString()); } catch (Exception ex) { return null; }
    }
}
