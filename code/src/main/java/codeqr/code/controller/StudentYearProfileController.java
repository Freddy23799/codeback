package codeqr.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional;

// import java.util.logging.StudentYearProfile;

import codeqr.code.model.StudentYearProfile;
import codeqr.code.service.StudentYearProfileService;
// --------------- StudentYearProfile Controller -----------------
@RestController
@RequestMapping("/api/admin/student-year-profiles")
public class StudentYearProfileController {

    private final StudentYearProfileService service;

    public StudentYearProfileController(StudentYearProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentYearProfile> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentYearProfile> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-student/{studentId}")
    public List<StudentYearProfile> getByStudent(@PathVariable Long studentId) {
        return service.findByStudentId(studentId);
    }

    @PostMapping
    public StudentYearProfile create(@RequestBody StudentYearProfile entity) {
        return service.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentYearProfile> update(@PathVariable Long id, @RequestBody StudentYearProfile entity) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
