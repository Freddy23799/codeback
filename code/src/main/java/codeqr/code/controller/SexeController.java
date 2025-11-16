package codeqr.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional;

import codeqr.code.model.*;
import codeqr.code.service.*;


// --------------- Sexe Controller -----------------
@RestController
@RequestMapping("/api/admin/sexes")
public class SexeController {

    private final SexeService service;

    public SexeController(SexeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Sexe> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sexe> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sexe create(@RequestBody Sexe entity) {
        return service.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sexe> update(@PathVariable Long id, @RequestBody Sexe entity) {
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