package codeqr.code.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

// import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.*;

import codeqr.code.model.Course;
// import codeqr.code.model.CourseReport;
// import codeqr.code.model.StudentYearProfile;
import io.lettuce.core.dynamic.annotation.Param;

// --- Course Repository ---
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
   
    // List<Course> findByLevelId(Long levelId);
    // List<Course> findBySpecialtyId(Long specialtyId);
   
 Page<Course> findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCase(String title, String code, Pageable pageable);
       Optional<Course> findByCode(String code);

    
// List<Course> findByTeacherYearProfie_Teacher_IdAndTeacherYearProfile_Level_IdAndTeacherYearProfile_Specialty_Id(
    // Long teacherId, Long levelId, Long specialtyId);

List<Course> findAllByIdIn(Collection<Long> ids);

//     @Query("SELECT c FROM Course c WHERE c.teacherYearProfile.id = :id")
// List<Course> findCoursesByTeacherYearProfileId(@Param("id") Long teacherYearProfileId);
}
