package codeqr.code.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import codeqr.code.dto.SessionDTO;
import codeqr.code.model.Enrollment;
import codeqr.code.service.EnrollmentService;
import codeqr.code.service.StudentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;

    /* ---- CRUD ---- */

    @GetMapping
    public ResponseEntity<List<Enrollment>> findAll() {
        return ResponseEntity.ok(enrollmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> get(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.get(id));
    }

    // Création par profileId
    @PostMapping
    public ResponseEntity<Enrollment> create(@RequestParam("studentYearProfileId") Long studentYearProfileId) {
        return ResponseEntity.ok(enrollmentService.create(studentYearProfileId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enrollment> update(@PathVariable Long id,
                                             @RequestParam(value = "studentYearProfileId", required = false) Long studentYearProfileId) {
        return ResponseEntity.ok(enrollmentService.update(id, studentYearProfileId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ---- Sessions ---- */

    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<SessionDTO>> getSessions(@PathVariable("id") Long enrollmentId) {
        List<SessionDTO> sessions = studentService.getSessionsForEnrollment(enrollmentId);
        return ResponseEntity.ok(sessions);
    }
}
