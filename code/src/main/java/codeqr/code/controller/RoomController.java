package codeqr.code.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional;

import codeqr.code.dto.RoomCreateDto;
import codeqr.code.dto.RoomDto;
import codeqr.code.model.Room;
import codeqr.code.service.RoomService;



// --------------- Room Controller -----------------
@RestController
@RequestMapping("/api/admin/rooms")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

   



   
    

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-campus/{campusId}")
    public List<Room> getByCampus(@PathVariable Long campusId) {
        return service.findByCampusId(campusId);
    }

    
    @GetMapping
    public List<RoomDto> list() { return service.listAll(); }

    @PostMapping
    public RoomDto create(@RequestBody RoomCreateDto in) { return service.create(in); }

    @PutMapping("/{id}")
    public RoomDto update(@PathVariable Long id, @RequestBody RoomCreateDto in) { return service.update(id, in); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
