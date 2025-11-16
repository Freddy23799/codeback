package codeqr.code.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codeqr.code.model.CourseReport;
import codeqr.code.model.StudentYearProfile;
import codeqr.code.repository.StudentYearProfileRepository;
import codeqr.code.service.CourseReportService;

@RestController
@RequestMapping("/api/course-reports")
   @PreAuthorize("hasRole('ADMIN','PROFESSEUR','ETUDIANT')")
public class CourseReportController {

    private final CourseReportService service;
    private final StudentYearProfileRepository profileRepository;

    public CourseReportController(CourseReportService service, StudentYearProfileRepository profileRepository) {
        this.service = service;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/student/{studentYearProfileId}")
    public List<CourseReport> getReportsForStudent(@PathVariable Long studentYearProfileId) {
        StudentYearProfile profile = profileRepository.findById(studentYearProfileId).orElseThrow();
        return service.getReportsForStudent(profile);
    }

    @PostMapping
    public CourseReport createReport(@RequestBody CourseReport report) {
        return service.saveReport(report);
    }
}