package codeqr.code.service;

import codeqr.code.model.CourseReport;
import codeqr.code.model.StudentYearProfile;
import codeqr.code.repository.CourseReportRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseReportService {

    private final CourseReportRepository repository;

    public CourseReportService(CourseReportRepository repository) {
        this.repository = repository;
    }

    public List<CourseReport> getReportsForStudent(StudentYearProfile profile) {
        return repository.findByStudentYearProfile(profile);
    }

    public CourseReport saveReport(CourseReport report) {
        return repository.save(report);
    }
}