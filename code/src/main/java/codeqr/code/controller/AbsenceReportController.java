  
    package codeqr.code.controller;

import codeqr.code.dto.AbsenceReportDTO;
import codeqr.code.service.AbsenceReportService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/absences")
@RequiredArgsConstructor
public class AbsenceReportController {

    private final AbsenceReportService absenceReportService;

    @GetMapping
    public List<AbsenceReportDTO> getAbsenceReport(
            @RequestParam Long academicYearId,
            @RequestParam Long levelId,
            @RequestParam Long specialtyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return absenceReportService.getAbsenceReport(academicYearId, levelId, specialtyId, start, end);
    }
}
    
    