package codeqr.code.controller;

import codeqr.code.dto.ProfessorTimetableResponse;
import codeqr.code.service.ProfessorTimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professeur")
@RequiredArgsConstructor
public class ProfessorTimetableController {

    private final ProfessorTimetableService timetableService;

    @GetMapping("/timetable/{profId}")
    public ResponseEntity<ProfessorTimetableResponse> getTimetableForProfessor(@PathVariable Long profId) {
        ProfessorTimetableResponse resp = timetableService.getTimetablesForProfessor(profId);
        return ResponseEntity.ok(resp);
    }
}
