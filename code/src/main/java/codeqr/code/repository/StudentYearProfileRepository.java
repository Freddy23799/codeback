package codeqr.code.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import codeqr.code.dto.ExpectedProfileDTO;
import codeqr.code.model.AcademicYear;
import codeqr.code.model.Level;
import codeqr.code.model.Specialty;
import codeqr.code.model.StudentYearProfile;
// --- StudentYearProfile Repository ---
@Repository
public interface StudentYearProfileRepository extends JpaRepository<StudentYearProfile, Long> {

    Optional<StudentYearProfile> findByStudentIdAndAcademicYearId(Long studentId, Long academicYearId);
 List<StudentYearProfile> findByLevelIdAndSpecialtyId(Long levelId, Long spesialtyId);
 List<StudentYearProfile> findByAcademicYear_IdAndLevel_IdAndSpecialty_IdAndActiveTrue(
    Long academicYearId, Long levelId, Long specialtyId
);
    @Query("SELECT syp FROM StudentYearProfile syp " +
       "WHERE syp.student.id = :studentId " +
       "AND syp.specialty.id = :specialtyId " +
       "AND syp.level.id = :levelId " +
       "AND syp.academicYear.id = :academicYearId")
Optional<StudentYearProfile> findByStudentIdAndSpecialtyAndLevelAndAcademicYear(
    @Param("studentId") Long studentId,
    @Param("specialtyId") Long specialtyId,
    @Param("levelId") Long levelId,
    @Param("academicYearId") Long academicYearId);

    
     List<StudentYearProfile> findByStudentUserUsername(String username);
@Query("SELECT DISTINCT s.level FROM StudentYearProfile s")
    List<Level> findAllLevels();

    @Query("SELECT DISTINCT s.academicYear FROM StudentYearProfile s")
    List<AcademicYear> findAllYears();

    @Query("SELECT COUNT(s) FROM StudentYearProfile s WHERE s.level.id = :levelId")
    Long countByLevel(@Param("levelId") Long levelId);

    @Query("SELECT COUNT(s) FROM StudentYearProfile s WHERE s.level.id = :levelId AND s.academicYear.id = :yearId")
    Long countByLevelAndYear(@Param("levelId") Long levelId, @Param("yearId") Long yearId);
    // Pour récupérer les profils par studentId
    // List<StudentYearProfile> findByStudentId(Long studentId);
    Object countByLevel(Level level);
    
@Query("SELECT p FROM StudentYearProfile p " +
           "JOIN FETCH p.student s " +
           "WHERE p.level.id = :levelId AND p.specialty.id = :specialtyId AND p.academicYear.id = :academicYearId")
    List<StudentYearProfile> findByLevelAndSpecialtyAndAcademicYearWithStudent(
            @Param("levelId") Long levelId,
            @Param("specialtyId") Long specialtyId,
            @Param("academicYearId") Long academicYearId);

    // List<StudentYearProfile> findByStudentId(Long studentId);
    // List<StudentYearProfile> findByStudentUserUsername(String username);
    // ajout d'opérations utilitaires si besoin (ex: find active by student)
    // List<StudentYearProfile> findByLevel_IdAndSpecialty_IdAndAcademicYear_Id(Long levelId, Long specialtyId, Long academicYearId);

     List<StudentYearProfile> findByLevelIdAndSpecialtyIdAndAcademicYearId(Long levelId, Long specialtyId, Long academicYearId);
    List<StudentYearProfile> findByStudentIdAndActiveTrue(Long studentId);
       List<StudentYearProfile>  findByLevelAndSpecialtyAndAcademicYear(Level level,Specialty specialty,AcademicYear academicYear);

  @Query("SELECT syp " +
           "FROM StudentYearProfile syp " +
           "JOIN FETCH syp.student st " +
           "JOIN FETCH st.user u " +
           "JOIN FETCH st.sexe sx " +
           "JOIN FETCH syp.level l " +
           "JOIN FETCH syp.specialty sp " +
           "LEFT JOIN FETCH sp.department d " +
           "JOIN FETCH syp.academicYear ay " +
           "WHERE l.id = :levelId " +
           "AND sp.id = :specialtyId " +
           "AND ay.id = :yearId")
    List<StudentYearProfile> findAllByLevelSpecialtyYear(
            @Param("levelId") Long levelId,
            @Param("specialtyId") Long specialtyId,
            @Param("yearId") Long yearId
    );

    // Tous les profiles pour un étudiant (utile pour afficher ses enrôlements)
    List<StudentYearProfile> findByStudentId(Long studentId);

    // Option pratique : trouver profile par student + academicYear + level + specialty
    Optional<StudentYearProfile> findByStudentIdAndAcademicYearIdAndLevelIdAndSpecialtyId(
        Long studentId, Long academicYearId, Long levelId, Long specialtyId);

    // Profils attendus pour un niveau/spécialité/année (utile pour marquage manuel)
    List<StudentYearProfile> findByAcademicYearIdAndLevelIdAndSpecialtyId(
        Long academicYearId, Long levelId, Long specialtyId);









        @Query("""
      SELECT new codeqr.code.dto.ExpectedProfileDTO(
        p.id,
        s.id,
        s.fullName,
        s.matricule,
        p.level.id,
        p.specialty.id,
        p.academicYear.id,
        a.status,
        a.source
      )
      FROM StudentYearProfile p
      LEFT JOIN p.student s
      LEFT JOIN Attendance a ON a.studentYearProfile = p AND a.session.id = :sessionId
      WHERE p.level.id = :levelId AND p.specialty.id = :specialtyId AND p.academicYear.id = :ayId
      ORDER BY s.fullName
    """)
    List<ExpectedProfileDTO> findExpectedProfilesForSession(
        @Param("sessionId") Long sessionId,
        @Param("levelId") Long levelId,
        @Param("specialtyId") Long specialtyId,
        @Param("ayId") Long academicYearId
    );
     Optional<StudentYearProfile> findTopByStudent_IdOrderByIdDesc(Long studentId);

}