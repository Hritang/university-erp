package com.university.erp.student;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TranscriptExporter {

    public static boolean exportCSV(String filePath, List<StudentDAO.GradeView> grades) {
        try (FileWriter writer = new FileWriter(filePath.endsWith(".csv") ? filePath : filePath + ".csv")) {


            writer.write("Code,Title,Component,Score,Final Grade\n");


            for (StudentDAO.GradeView g : grades) {
                writer.write(String.format(
                        "%s,%s,%s,%.2f,%s\n",
                        csvEscape(g.code),
                        csvEscape(g.title),
                        csvEscape(g.component),
                        g.score,
                        csvEscape(g.finalGrade == null ? "-" : g.finalGrade)
                ));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static String csvEscape(String val) {
        if (val == null) return "";
        boolean mustQuote = val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r");
        String v = val.replace("\"", "\"\"");
        return mustQuote ? "\"" + v + "\"" : v;
    }
}
