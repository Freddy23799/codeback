package codeqr.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional;

import codeqr.code.dto.CampusDto;
import codeqr.code.model.Campus;
import codeqr.code.service.CampusService;


// --------------- Campus Controller -----------------
@RestController
@RequestMapping("/api/admin/campuses")
public class CampusController {

    private final CampusService service;

    public CampusController(CampusService service) {
        this.service = service;
    }

    @GetMapping
    public List<CampusDto> list() { return service.listAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Campus> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Campus create(@RequestBody Campus entity) {
        return service.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campus> update(@PathVariable Long id, @RequestBody Campus entity) {
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