package codeqr.code.controller;

import codeqr.code.dto.StudentTimetableResponse;
import codeqr.code.service.StudentTimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etudiant/timetable")
@RequiredArgsConstructor
public class EtudiantTimetableController {

    private final StudentTimetableService service;

    /**
     * Retourne les emplois du temps correspondant au profil étudiant le plus récent.
     * Le front envoie maintenant studentId (l'id du profil Student).
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentTimetableResponse> getForStudent(@PathVariable Long studentId) {
        StudentTimetableResponse resp = service.getTimetablesForStudent(studentId);
        return ResponseEntity.ok(resp);
    }
}
