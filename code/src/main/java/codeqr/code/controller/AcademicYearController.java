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

import codeqr.code.dto.AcademicYearCreateDto;
import codeqr.code.dto.AcademicYearDto;
import codeqr.code.model.AcademicYear;
import codeqr.code.service.AcademicYearService;
import lombok.RequiredArgsConstructor;

// DTO pour exposer côté front

@RestController
@RequestMapping("/api/admin/academic-years")
@RequiredArgsConstructor
@CrossOrigin
public class AcademicYearController {

    private final AcademicYearService service;

    // Conversion entity -> DTO
    
   

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        return service.findById(id)
                .map(ay -> ResponseEntity.ok(toDto(ay)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Object toDto(AcademicYear ay) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDto'");
    }

    @GetMapping
    public List<AcademicYearDto> list() {
        return service.listAll();
    }

    @PostMapping
    public AcademicYearDto create(@RequestBody AcademicYearCreateDto in) {
        return service.create(in);
    }

    @PutMapping("/{id}")
    public AcademicYearDto update(@PathVariable Long id, @RequestBody AcademicYearCreateDto in) {
        return service.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
