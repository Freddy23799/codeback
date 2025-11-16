package codeqr.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import codeqr.code.dto.DashboardSummaryDTO;
import codeqr.code.service.DashboardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/students/{studentId}/dashboard
     * Retourne les stats + notifications pour le dashboard étudiant.
     */
    
    @GetMapping("/{studentId}/dashboard")
    public ResponseEntity<DashboardSummaryDTO> getStudentDashboard(@PathVariable Long studentId) {
        DashboardSummaryDTO dto = dashboardService.getDashboardForStudent(studentId);
        return ResponseEntity.ok(dto);
    }
}
