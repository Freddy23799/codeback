package codeqr.code.controller;

import codeqr.code.dto.AttendanceDTOS;
import codeqr.code.dto.PageResponses;
import codeqr.code.dto.SessionsPageDTO;
import codeqr.code.service.SessionService;
import codeqr.code.service.SurveillantServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveillants")
@RequiredArgsConstructor
public class SurveillantController {

    private final SurveillantServices service;
    private final SessionService sessionService;

    /**
     * Récupérer les sessions pour un surveillant avec filtres et pagination
     */
    @GetMapping("/{surveillantId}/sessions")
    public SessionsPageDTO getSessions(
            @PathVariable("surveillantId") Long surveillantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "teacherName", required = false) String teacherName,
            @RequestParam(value = "specialtyId", required = false) Long specialtyId,
            @RequestParam(value = "levelId", required = false) Long levelId
    ) {
        return sessionService.getSessionsForSurveillant(
                surveillantId, teacherName, specialtyId, levelId, page, size
        );
    }

    /**
     * Récupérer les présences (attendances) d'une session
     */
    @GetMapping("/sessions/{sessionId}/attendances")
    public PageResponses<AttendanceDTOS> sessionAttendances(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String studentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "105") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return service.getAttendancesForSession(sessionId, studentName, pageable);
    }
}
