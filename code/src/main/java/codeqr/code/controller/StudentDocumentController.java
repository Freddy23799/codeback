package codeqr.code.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import codeqr.code.dto.StudentDocumentDto;
import codeqr.code.service.StudentDocumentService;

@RestController
@RequestMapping("/api/student")
public class StudentDocumentController {

    private final StudentDocumentService service;

    public StudentDocumentController(StudentDocumentService service) {
        this.service = service;
    }

    @GetMapping("/documents")
    public ResponseEntity<Page<StudentDocumentDto>> listForStudent(
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            Principal principal,
            @RequestParam Optional<String> category,
            @RequestParam Optional<Long> courseId,
            @RequestParam Optional<Long> levelId,
            @RequestParam Optional<Long> yearId,
            @RequestParam Optional<String> q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        String username = headerUsername != null ? headerUsername : (principal != null ? principal.getName() : null);
        if (username == null) {
            return ResponseEntity.badRequest().build();
        }

        Page<StudentDocumentDto> res = service.listForStudent(username, category, courseId, levelId, yearId, q, page, size);
        return ResponseEntity.ok(res);
    }
}
