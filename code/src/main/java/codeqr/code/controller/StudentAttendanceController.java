package codeqr.code.controller;

import codeqr.code.dto.AttendanceHistoryFilter;
import codeqr.code.dto.AttendanceHistoryRowDTO;
import codeqr.code.service.StudentAttendanceService;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController

@RequestMapping("api/students")
public class StudentAttendanceController {

    private final StudentAttendanceService service;

    public StudentAttendanceController(StudentAttendanceService service) {
        this.service = service;
    }

    @GetMapping("/{studentId}/attendance/history")
    public Page<AttendanceHistoryRowDTO> history(
            @PathVariable Long studentId,
            @RequestParam(required = false) String period,                 // this_week | this_month | custom
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,                 // PRESENT/ABSENT/PENDING
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort                    // ex: sessionStart,desc
    ) {
        // Pageable
        Pageable pageable = resolvePageable(page, size, sort);

        // Filtre
        var filter = new AttendanceHistoryFilter();
        filter.period = period;
        filter.from = from;
        filter.to = to;
        filter.courseId = courseId;
        filter.campusId = campusId;
        filter.roomId = roomId;
        filter.status = status;
        filter.q = q;

        return service.history(studentId, filter, pageable);
    }

    private Pageable resolvePageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Order.desc("sessionStart")));
        }
        // quasar envoie "field,desc" ou "field,asc"
        String[] parts = sort.split(",");
        String prop = parts[0];
        boolean desc = parts.length > 1 && parts[1].equalsIgnoreCase("desc");
        return PageRequest.of(page, size, desc ? Sort.by(prop).descending() : Sort.by(prop).ascending());
    }
}
