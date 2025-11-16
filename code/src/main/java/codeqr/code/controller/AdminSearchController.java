
package codeqr.code.controller;

import codeqr.code.dto.*;
import codeqr.code.service.interfaces.*;
import codeqr.code.service.interfaces.ProfessorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admins")
public class AdminSearchController {

    private final CourseService courseService;
    private final ProfessorService professorService;
    private final RoomService roomService;

    @Autowired
    public AdminSearchController(CourseService courseService, ProfessorService professorService, RoomService roomService) {
        this.courseService = courseService;
        this.professorService = professorService;
        this.roomService = roomService;
    }

    @GetMapping("/courses")
    public ResponseEntity<PagedResponses<CourseDto>> searchCourses(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Page<CourseDto> p = courseService.search(q, page, limit);
        return ResponseEntity.ok(new PagedResponses<>(p.getContent(), p.getTotalElements()));
    }

    @GetMapping("/professors")
    public ResponseEntity<PagedResponses<ProfessorDto>> searchProfessors(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Page<ProfessorDto> p = professorService.search(q, page, limit);
        return ResponseEntity.ok(new PagedResponses<>(p.getContent(), p.getTotalElements()));
    }

    @GetMapping("/rooms")
    public ResponseEntity<PagedResponses<RoomDtos>> searchRooms(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Page<RoomDtos> p = roomService.search(q, page, limit);
        return ResponseEntity.ok(new PagedResponses<>(p.getContent(), p.getTotalElements()));
    }
}
