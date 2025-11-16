

package codeqr.code.controller;

import codeqr.code.dto.*;
import codeqr.code.dto.SessionListDTO;
import codeqr.code.dto.StudentListDTO;
import codeqr.code.model.Attendance;
import codeqr.code.model.Student;
import codeqr.code.repository.AttendanceRepository;
import codeqr.code.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import codeqr.code.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class StudentsController {

    @Autowired
    private StudentService service;
   private  AttendanceRepository attendanceRepository;
    // GET /api/students?cursor=...&limit=50&q=...&specialty=...&level=...
    @GetMapping("/student")
    public List<StudentListDTO> students(
            @RequestParam(required=false) Long cursor,
            @RequestParam(defaultValue="50") int limit,
            @RequestParam(required=false) String q,
            @RequestParam(required=false) Long specialty,
            @RequestParam(required=false) Long level
    ) {
        return service.listStudents(cursor, limit, q, specialty, level);
    }

    // GET /api/students/{id}/enrollments?offset=0&limit=20
    @GetMapping("/student/{id}/enrollments")
    public List<EnrollmentListDTO> enrollments(
            @PathVariable("id") Long studentId,
            @RequestParam(defaultValue="0") int offset,
            @RequestParam(defaultValue="20") int limit
    ) {
        return service.listEnrollments(studentId, offset, limit);
    }

    // GET /api/enrollments/{id}/sessions?start=2025-01-01T00:00&end=2025-02-01T00:00&lastStart=...&lastId=...&limit=100
    @GetMapping("/enrollment/{id}/sessions")
    public List<SessionListDTO> sessions(
            @PathVariable("id") Long studentYearProfileId,
            @RequestParam(required=false) String start,
            @RequestParam(required=false) String end,
            @RequestParam(required=false) String lastStart,
            @RequestParam(required=false) Long lastId,
            @RequestParam(defaultValue="100") int limit
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime s = start != null ? LocalDateTime.parse(start, fmt) : null;
        LocalDateTime e = end != null ? LocalDateTime.parse(end, fmt) : null;
        LocalDateTime ls = lastStart != null ? LocalDateTime.parse(lastStart, fmt) : null;
        return service.listSessions(studentYearProfileId, s, e, ls, lastId, limit);
    }







    @GetMapping("/reports/enrollment/{id}/pdf")
    public ResponseEntity<byte[]> generateEnrollmentPdf(
            @PathVariable("id") Long studentYearProfileId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            // Récupérer toutes les sessions de cet étudiant
            List<SessionListDTO> sessions = service.listSessions(studentYearProfileId, start, end, null, null, 5000);

            // Créer le PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Titre
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(30, 60, 120));
            document.add(new Paragraph("Historique des sessions", titleFont));
            document.add(new Paragraph(" ")); // saut de ligne

            // Tableau
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 3, 2, 2, 3, 3, 2});

            // En-têtes
            addHeader(table, "#");
            addHeader(table, "Cours");
            addHeader(table, "Campus");
            addHeader(table, "Salle");
            addHeader(table, "Professeur");
            addHeader(table, "Début");
            addHeader(table, "Présences");

            int i = 1;
            for (SessionListDTO s : sessions) {
                table.addCell(String.valueOf(i++));
                table.addCell(s.courseTitle != null ? s.courseTitle : "");
                table.addCell(s.campusName != null ? s.campusName : "");
                table.addCell(s.roomName != null ? s.roomName : "");
                table.addCell(s.teacherName != null ? s.teacherName : "");
                table.addCell(s.startTime != null ? s.startTime.toString() : "");
                table.addCell(s.attendanceCount != null ? s.attendanceCount.toString() : "");
            }

            document.add(table);

            document.close();

            // Retour en réponse HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sessions.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addHeader(PdfPTable table, String text) {
        Font headFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        PdfPCell hcell = new PdfPCell(new Phrase(text, headFont));
        hcell.setBackgroundColor(new Color(41, 128, 185));
        hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(hcell);
    }




























  

//    @GetMapping("/session/{sessionId}/students")
// public ResponseEntity<?> getStudentsOfSession(@PathVariable Long sessionId,
//                                               @RequestParam(defaultValue = "1") int page,
//                                               @RequestParam(defaultValue = "50") int size) {
//     // la page côté front est 1-based -> convertit en 0-based pour Spring Data
//     int pg = Math.max(1, page) - 1;

//     // récupère une Page<Attendance>
//     Page<Attendance> p = attendanceRepository.findBySessionId(sessionId, PageRequest.of(pg, size));

//     // mappe chaque Attendance -> StudentDto (évite d'exposer l'entité JPA directement)
//     List<StudentDtos> items = p.getContent().stream()
//         .map(att -> mapToStudentDtos(att.getStudent()))
//         .collect(Collectors.toList());

//     return ResponseEntity.ok(Map.of(
//         "items", items,
//         "total", p.getTotalElements()
//     ));
// }

// // méthode de mapping (privée dans le controller ou dans un util/service)
// private StudentDtos mapToStudentDtos(Student s) {
//     if (s == null) return null;
//     StudentDtos dto = new StudentDtos();
//     dto.setId(s.getId());
//     dto.setFirstName(s.getFirstName()); // adapte aux noms de champs réels
//     dto.setLastName(s.getLastName());
//     dto.setEmail(s.getEmail());
//     // ajoute d'autres champs utiles (matricule, phone...) selon ton modèle
//     return dto;
// }
}
