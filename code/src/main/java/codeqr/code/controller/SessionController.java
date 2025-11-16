



package codeqr.code.controller;

import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;

import codeqr.code.dto.*;
import codeqr.code.model.*;
import codeqr.code.service.*;
import lombok.RequiredArgsConstructor;
import codeqr.code.repository.*;
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
 private final UserService userService;
    // ------------------- CRUD basique -------------------

    // @GetMapping
    // public ResponseEntity<List<Session>> getAllSessions() {
    //     return ResponseEntity.ok(sessionService.findAll());
    // }





    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable Long id) {
        return sessionService.getSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }




    @PostMapping("/surveillant")

    public ResponseEntity<Session2DTO> createSessions(@RequestBody CreateSessionRequest req) {
        Session2DTO dto = sessionService.createSessionAtomic(req);
        return ResponseEntity.ok(dto);
    }
    @PostMapping
    public ResponseEntity<Session2DTO> createSession(@RequestBody CreateSessionRequest req) {
        return ResponseEntity.ok(sessionService.createSessionAtomic(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session payload) {
        return ResponseEntity.ok(sessionService.update(id, payload));
    }






    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.cancelSession(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Gestion QR et présence -------------------

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Void> endSession(@PathVariable Long sessionId) {
        sessionService.endSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/scan")
    public ResponseEntity<AttendanceDTO> recordScan(@RequestBody ScanRequest req) {
        return ResponseEntity.ok(sessionService.recordScan(req));
    }

    @PostMapping("/scan/bulk")
    public ResponseEntity<List<AttendanceDTO>> recordScansBulk(@RequestBody List<ScanRequest> scans) {
        return ResponseEntity.ok(sessionService.recordScansBulk(scans));
    }

    // ------------------- Marquage manuel -------------------

    
   @PostMapping("/{sessionId}/markManual")
public ResponseEntity<Void> markManualAttendance(
        @PathVariable Long sessionId,
        @RequestBody List<Long> presentProfileIds
) {
    sessionService.markManualAttendance(sessionId, presentProfileIds);
    return ResponseEntity.ok().build();
}

@GetMapping("/{sessionId}/expectedProfiles")
public ResponseEntity<List<ExpectedProfileDTO>> getExpectedProfiles(@PathVariable Long sessionId) {
    return ResponseEntity.ok(sessionService.getExpectedProfilesForSession(sessionId));
}






  @GetMapping("/{username}/sessions")
    public SessionPageResponse getTeacherSessions(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return sessionService.getSessionsByUsername(username, search, pageable);
    }

}
