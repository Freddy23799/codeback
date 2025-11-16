
package codeqr.code.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codeqr.code.dto.DashboardDto;
import codeqr.code.dto.PageResponse;
import codeqr.code.dto.SessionDtos;
import codeqr.code.repository.SessionRepository;
import codeqr.code.service.SessionService;
import codeqr.code.service.TeacherService;
import codeqr.code.service.TeacherSessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherSessionController {

    private final TeacherService teacherService;
    private final TeacherSessionService teacherSessionService;
    private final SessionRepository sessionRepository;
      private final SessionService sessionService;

    // Récupération des sessions paginées pour un teacher
    @GetMapping("/{teacherId}/sessions")
    public PageResponse<SessionDtos> getTeacherSessions(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return teacherSessionService.getSessionsForTeacher(teacherId, search, date, pageable);
    }

    // Récupération des stats pour le dashboard
    @GetMapping("/dashboard/{id}")
    public DashboardDto getDashboard(@PathVariable Long id) {
        return sessionService.buildDashboard(id);
    }

   
}
