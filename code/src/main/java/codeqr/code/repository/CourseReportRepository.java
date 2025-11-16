package codeqr.code.repository;

import codeqr.code.model.Course;
import codeqr.code.model.CourseReport;
import codeqr.code.model.StudentYearProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CourseReportRepository extends JpaRepository<CourseReport, Long> {
    List<CourseReport> findByStudentYearProfile(StudentYearProfile profile);

   
}