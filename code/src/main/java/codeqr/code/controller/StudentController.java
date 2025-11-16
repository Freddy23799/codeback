package codeqr.code.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codeqr.code.dto.EnrollmentDTO;
import codeqr.code.dto.SessionDTO;
import codeqr.code.dto.StudentDTO;
import codeqr.code.dto.StudentRequest;
import codeqr.code.dto.StudentWithEnrollmentsDTO;
import codeqr.code.service.StudentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class StudentController {

    private final StudentService studentService;

    // -------------------- Students --------------------
    @GetMapping("/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentDTO> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @PostMapping("/students")
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentRequest req) {
        return ResponseEntity.status(201).body(studentService.createStudent(req));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @RequestBody StudentRequest req) {
        return ResponseEntity.ok(studentService.updateStudent(id, req));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------- Enrollments --------------------
    // @GetMapping("/students/{studentId}/enrollments")
    // public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByStudent(@PathVariable Long studentId) {
    //     return ResponseEntity.ok(studentService.getEnrollmentsByStudent(studentId));
    // }

    // @PostMapping("/students/{studentId}/enrollments")
    // public ResponseEntity<EnrollmentDTO> addEnrollment(
    //         @PathVariable Long studentId,
    //         @RequestBody EnrollmentRequest body
    // ) {
    //     return ResponseEntity.status(201).body(
    //             studentService.addEnrollment(studentId, body.getSpecialtyId(), body.getLevelId(), body.getAcademicYearId())
    //     );
    // }

    // @PutMapping("/enrollments/{enrollmentId}")
    // public ResponseEntity<EnrollmentDTO> updateEnrollment(
    //         @PathVariable Long enrollmentId,
    //         @RequestBody EnrollmentRequest body
    // ) {
    //     return ResponseEntity.ok(
    //             studentService.updateEnrollment(enrollmentId, body.getSpecialtyId(), body.getLevelId(), body.getAcademicYearId())
    //     );
    // }

    // @DeleteMapping("/enrollments/{enrollmentId}")
    // public ResponseEntity<Void> deleteEnrollment(@PathVariable Long enrollmentId) {
    //     studentService.deleteEnrollment(enrollmentId);
    //     return ResponseEntity.noContent().build();
    // }










      @GetMapping("/students/full")
    public ResponseEntity<List<StudentWithEnrollmentsDTO>> getStudentsFull() {
        return ResponseEntity.ok(studentService.getAllStudentsWithEnrollments());
    }

    @PostMapping("/students/{id}/enrollments")
    public ResponseEntity<EnrollmentDTO> addEnrollment(@PathVariable Long id, @RequestBody EnrollmentRequest req) {
        return ResponseEntity.ok(studentService.addEnrollment(id, req.getSpecialtyId(), req.getLevelId(), req.getAcademicYearId()));
    }

    // @PutMapping("/enrollments/{id}")
    // public ResponseEntity<EnrollmentDTO> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentRequest req) {
    //     return ResponseEntity.ok(studentService.updateEnrollment(id, req.getSpecialtyId(), req.getLevelId(), req.getAcademicYearId()));
    // }

    // @DeleteMapping("/enrollments/{id}")
    // public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
    //     studentService.deleteEnrollment(id);
    //     return ResponseEntity.ok().build();
    // }
    // // -------------------- Sessions for an Enrollment --------------------
    // @GetMapping("/enrollments/{enrollmentId}/sessions")
    // public ResponseEntity<List<SessionDTO>> getSessionsForEnrollment(@PathVariable Long enrollmentId) {
    //     return ResponseEntity.ok(studentService.getSessionsForEnrollment(enrollmentId));
    // }

    // -------------------- Request DTO for enrollments --------------------
    public static class EnrollmentRequest {
        private Long specialtyId;
        private Long levelId;
        private Long academicYearId;

        public Long getSpecialtyId() { return specialtyId; }
        public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }

        public Long getLevelId() { return levelId; }
        public void setLevelId(Long levelId) { this.levelId = levelId; }

        public Long getAcademicYearId() { return academicYearId; }
        public void setAcademicYearId(Long academicYearId) { this.academicYearId = academicYearId; }
    }
}
