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

import codeqr.code.dto.LevelDto;
import codeqr.code.model.Level;
import codeqr.code.service.LevelService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/levels")
@RequiredArgsConstructor
@CrossOrigin
public class LevelController {

    private final LevelService service;
   @GetMapping
    public List<LevelDto> list() { return service.listAll(); }
    @GetMapping("/{id}")
    public ResponseEntity<Level> get(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Level> create(@RequestBody Level payload) {
        payload.setId(null);
        Level saved = service.save(payload);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Level> update(@PathVariable Long id, @RequestBody Level payload) {
        return service.findById(id)
                .map(existing -> {
                    payload.setId(id);
                    Level saved = service.save(payload);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
