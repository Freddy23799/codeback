package codeqr.code.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import codeqr.code.dto.TeacherSessionReportDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeacherSessionReportController {

    private final TeacherSessionReportService service;
  private final SessionService sessionService;
    @GetMapping("/admin/teacher-sessions")
    public List<TeacherSessionReportDTO> getTeacherSessions(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo
    ) {
        return service.getTeacherReports(specialty, level, academicYearId, dateFrom, dateTo);
    }






    //  @GetMapping("/seances")
    // public ResponseEntity<?> getSeances(
    //         @RequestParam("username") String username,
    //         @RequestParam(value = "page", defaultValue = "1") int page, // front envoie page 1-based
    //         @RequestParam(value = "size", defaultValue = "30") int size,
    //         @RequestParam(value = "search", required = false) String search,
    //         @RequestParam(value = "date", required = false) LocalDate dateIso
    // ) {
    //     Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);

    //     try {
    //         Map<String, Object> result = sessionService.getSeancesForUsername(username, search, dateIso, pageable);
    //         return ResponseEntity.ok(result);
    //     } catch (IllegalArgumentException ex) {
    //         return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    //     } catch (NoSuchElementException ex) {
    //         return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    //     } catch (Exception ex) {
    //         return ResponseEntity.status(500).body(Map.of("error", "Erreur serveur lors de la récupération des séances"));
    //     }
    // }
}





