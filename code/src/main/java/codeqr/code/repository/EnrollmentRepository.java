package codeqr.code.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import codeqr.code.dto.EnrollmentLightDTO;
import codeqr.code.dto2.EnrollmentCount;
import codeqr.code.model.Enrollment;
import codeqr.code.model.StudentYearProfile;
// --- Enrollment Repository ---
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentYearProfileId(Long studentYearProfileId);
    //  List<Enrollment> findByStudentYearProfile(StudentYearProfile studentYearProfile);   
    List<Enrollment> findByStudentYearProfile(StudentYearProfile studentYearProfile);
    // Optional<Enrollment> findByStudentYearProfileIdAndCourseId(Long studentYearProfileId, Long courseId);
    @Query("SELECT new codeqr.code.dto2.EnrollmentCount(e.studentYearProfile.level.name, COUNT(e)) " +
           "FROM Enrollment e " +
           "GROUP BY e.studentYearProfile.level.name")
    List<EnrollmentCount> findAllLevelsWithCounts();
    @Query("select e from Enrollment e where e.studentYearProfile.student.id = :studentId")
List<Enrollment> findByStudentId(@Param("studentId") Long studentId);



@Query("select new codeqr.code.dto.EnrollmentLightDTO(e.id, e.studentYearProfile.student.id, e.studentYearProfile.id, " +
"e.studentYearProfile.specialty.id, e.studentYearProfile.specialty.name, " +
"e.studentYearProfile.level.id, e.studentYearProfile.level.name, " +
"e.studentYearProfile.academicYear.id, e.studentYearProfile.academicYear.label) " +
"from Enrollment e where e.studentYearProfile.student.id in :studentIds")
List<EnrollmentLightDTO> findByStudentIds(@Param("studentIds") List<Long> studentIds);

    // Optional find pour vérification
    Optional<Enrollment> findById(Long id);


     List<Enrollment> findByStudentYearProfile_Student_Id(Long studentId);
}
