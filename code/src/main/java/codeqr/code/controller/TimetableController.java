package codeqr.code.controller;

import codeqr.code.dto.TimetableWeekResponse;
import codeqr.code.service.TimetableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetables")
@Slf4j
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /**
     * GET /api/timetables/last-week?userId=...
     */
    @GetMapping("/last-week")
    public ResponseEntity<?> getLastWeek(@RequestParam("userId") Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Paramètre userId requis");
        }
        try {
            TimetableWeekResponse resp = timetableService.getLastWeekForUser(userId);
            if (resp == null) {
                return ResponseEntity.noContent().build(); // 204 si rien
            }
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Error while fetching last week for userId={}", userId, ex);
            return ResponseEntity.status(500).body("Erreur serveur lors de la récupération de la dernière semaine");
        }
    }
}
