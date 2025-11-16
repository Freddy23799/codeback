package codeqr.code.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import codeqr.code.dto.Attendanceto;
import codeqr.code.dto.AttendanceDTO;
import codeqr.code.dto.AttendanceScanRequest;
import codeqr.code.dto.ScanRequest;
import codeqr.code.model.Attendance;
// import codeqr.code.model.StudentYearProfile;
import codeqr.code.service.AttendanceService;
import codeqr.code.service.SessionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SessionService sessionService;

    // ---------- CRUD basique sur Attendance ----------
    // @GetMapping
    // public ResponseEntity<List<Attendance>> getAll() {
    //     return ResponseEntity.ok(attendanceService.findAll());
    // }

    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getById(@PathVariable Long id) {
        return attendanceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Attendance> create(@RequestBody Attendance entity) {
        Attendance saved = attendanceService.save(entity);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Attendance> update(@PathVariable Long id, @RequestBody Attendance entity) {
        if (!attendanceService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        Attendance updated = attendanceService.save(entity);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!attendanceService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Bulk sync : front envoie liste de ScanRequest (offline -> sync) ----------
    @PostMapping("/sync")
    public ResponseEntity<List<AttendanceDTO>> syncBulk(@RequestBody List<ScanRequest> requests) {
        List<AttendanceDTO> results = sessionService.recordScansBulk(requests);
        return ResponseEntity.ok(results);
    }

    // ---------- Marquage manuel : prof coche une liste de profils pour une session ----------
    

    // ---------- Obtenir la liste des profils attendus pour une session (pour UI cochers) ----------
    @PostMapping("/scan")
    
    public ResponseEntity<String> scanAttendance(@RequestBody AttendanceScanRequest request) {
        System.out.println("==== Reçu dans DTO ====");
System.out.println("SessionId: " + request.getSessionId());
System.out.println("StudentId: " + request.getStudentId());
System.out.println("QrToken: " + request.getQrToken());
System.out.println("ScannedAt: " + request.getScannedAt());
       attendanceService.processScan(request);
        return ResponseEntity.ok("etudiant marquer present avec succes");
    }

   


 @GetMapping("/{sessionId}/attendance")
    public Page<Attendanceto> getAttendances(
        @PathVariable Long sessionId,
        @RequestParam(defaultValue = "") String studentName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return attendanceService.getAttendances(sessionId, studentName, pageable);
    }






     @GetMapping
    public List<Attendance> getAll() {
        return attendanceService.findAll();
    }

    @PostMapping("/manual-mark/{sessionId}")
    public ResponseEntity<Map<String,String>> manualMark(
            @PathVariable Long sessionId,
            @RequestBody ManualMarkRequest request
    ) {
        attendanceService.markManual(sessionId, request.getStudentIds(), request.getStatus());
        return ResponseEntity.ok(Map.of("message","Présences mises à jour"));
    }


    
    @Data
    public static class ManualMarkRequest {
        private List<Long> studentIds;
        private Attendance.Status status;
    }
}
