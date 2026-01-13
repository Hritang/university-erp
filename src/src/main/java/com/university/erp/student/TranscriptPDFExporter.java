package com.university.erp.student;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.List;
import java.awt.Color;

public class TranscriptPDFExporter {

    public static boolean exportPDF(String filePath, List<StudentDAO.GradeView> grades, String username) {
        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float currentY = yStart;


            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
            cs.newLineAtOffset(margin, currentY);
            cs.showText("Transcript Report");
            cs.endText();

            currentY -= 30;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(margin, currentY);
            cs.showText("Student: " + username);
            cs.endText();

            currentY -= 40;


            float yPosition = currentY;
            float[] colX = {50, 130, 280, 420, 480};
            String[] headers = {"Code", "Title", "Component", "Score", "Final Grade"};


            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            for (int i = 0; i < headers.length; i++) {
                drawTextAt(cs, headers[i], colX[i], yPosition);
            }


            yPosition -= 5;
            cs.setStrokingColor(Color.BLACK);
            cs.setLineWidth(1f);
            cs.moveTo(margin, yPosition);
            cs.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            cs.stroke();

            yPosition -= 20;


            cs.setFont(PDType1Font.HELVETICA, 10);

            for (StudentDAO.GradeView g : grades) {

                if (yPosition < margin) break;

                drawTextAt(cs, g.code, colX[0], yPosition);
                drawTextAt(cs, shorten(g.title, 20), colX[1], yPosition);
                drawTextAt(cs, shorten(g.component, 20), colX[2], yPosition);


                String scoreStr = String.format("%.2f", g.score);
                drawTextAt(cs, scoreStr, colX[3], yPosition);

                String gradeStr = (g.finalGrade == null) ? "-" : g.finalGrade;
                drawTextAt(cs, gradeStr, colX[4], yPosition);

                yPosition -= 15;
            }

            cs.close();
            doc.save(filePath);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static void drawTextAt(PDPageContentStream cs, String text, float x, float y) throws IOException {
        if (text == null) return;
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private static String shorten(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "..." : text;
    }
}