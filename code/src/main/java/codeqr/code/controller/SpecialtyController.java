package codeqr.code.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import codeqr.code.model.Specialty;
import codeqr.code.dto.SpecialtyCreateDto;
import codeqr.code.dto.SpecialtyDto;
import codeqr.code.service.SpecialtyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/specialties")
@RequiredArgsConstructor
@CrossOrigin
public class SpecialtyController {

    private final SpecialtyService service;

  

    @GetMapping("/{id}")
    public ResponseEntity<Specialty> get(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-department/{departmentId}")
    public ResponseEntity<List<Specialty>> byDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(service.findByDepartmentId(departmentId));
    }

     @GetMapping
    public List<SpecialtyDto> list() { return service.listAll(); }

    @PostMapping
    public SpecialtyDto create(@RequestBody SpecialtyCreateDto in) { return service.create(in); }

    @PutMapping("/{id}")
    public SpecialtyDto update(@PathVariable Long id, @RequestBody SpecialtyCreateDto in) { return service.update(id, in); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
