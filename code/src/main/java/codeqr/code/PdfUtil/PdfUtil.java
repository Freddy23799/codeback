package codeqr.code.PdfUtil;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import codeqr.code.dto.CourseStatsDto;
import codeqr.code.dto.CampusCountDto;
import codeqr.code.dto.SessionSampleDto;

public class PdfUtil {

    // Version existante avec Map
    public static byte[] buildSimplePdf(Map<String, Object> stats) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Statistiques"));
            document.add(new Paragraph("-----------------------------"));

            // Parcours et affichage des stats
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                document.add(new Paragraph(entry.getKey() + " : " + entry.getValue()));
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    // 🚀 Nouvelle surcharge pour CourseStatsDto
    public static byte[] buildSimplePdf(CourseStatsDto stats) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("📊 Statistiques du cours"));
            document.add(new Paragraph("-----------------------------"));

            // Infos générales
            document.add(new Paragraph("Enseignant : " + stats.getTeacherName() + " (ID " + stats.getTeacherId() + ")"));
            document.add(new Paragraph("Total de séances : " + stats.getTotalSessions()));
            document.add(new Paragraph("Période : " + stats.getPeriodStart() + " → " + stats.getPeriodEnd()));
            document.add(new Paragraph(" ")); // saut de ligne

            // Répartition par campus
            document.add(new Paragraph("📍 Répartition par campus :"));
            if (stats.getSessionsByCampus() != null) {
                for (CampusCountDto campus : stats.getSessionsByCampus()) {
                    document.add(new Paragraph(" - " + campus.getCampusName() + " : " + campus.getCount() + " séances"));
                }
            }

            document.add(new Paragraph(" ")); // saut de ligne

            // Exemples de séances
            document.add(new Paragraph("📝 Exemple de séances :"));
            if (stats.getSessionsSample() != null) {
                for (SessionSampleDto sample : stats.getSessionsSample()) {
                    document.add(new Paragraph(
                        " - " + sample.getCourseTitle() +
                        " | " + sample.getStartTime() +
                        " | Salle : " + sample.getRoomName()
                    ));
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}
