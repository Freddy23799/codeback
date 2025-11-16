package codeqr.code.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import codeqr.code.model.TeacherYearProfile;

// --- TeacherYearProfile Repository ---
@Repository
public interface TeacherYearProfileRepository extends JpaRepository<TeacherYearProfile, Long> {


    List<TeacherYearProfile> findByTeacherId(Long teacherId);

    Optional<TeacherYearProfile> findByTeacherIdAndAcademicYearId(Long teacherId, Long academicYearId);

    //  List<TeacherYearProfile> findByTeacherId(Long teacherId);
    List<TeacherYearProfile> findByTeacherIdAndActiveTrue(Long teacherId);


 Optional<TeacherYearProfile> findByTeacher_User_Username(String username);

    // Pour chercher par userId via Student → User
    Optional<TeacherYearProfile> findByTeacher_User_Id(Long userId);

    @Override
    Optional<TeacherYearProfile> findById(Long id);
}
