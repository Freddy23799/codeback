package codeqr.code.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import codeqr.code.dto.*;
import codeqr.code.model.*;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
        select s from Student s
        where lower(s.fullName) like lower(concat('%', ?1, '%'))
           or lower(s.matricule) like lower(concat('%', ?1, '%'))
           or lower(s.email) like lower(concat('%', ?1, '%'))
    """)
   

    @EntityGraph(attributePaths = {
        "studentYearProfiles",
        "studentYearProfiles.enrollments",
        "studentYearProfiles.academicYear",
        "studentYearProfiles.level",
        "studentYearProfiles.specialty"
    })


    Optional<Student> findByEmail(String email);
    Optional<Student> findByMatricule(String matricule);
    Optional<Student> findByUserUsername(String username);
    
        Optional<Student> findById(Long id);
 Optional<Student> findByUser(User user);



@Query("select new codeqr.code.dto.StudentListDTO(s.id, s.fullName, s.matricule, s.email, s.sexe.id, s.sexe.name) from Student s")
Page<StudentListDTO> findAllAsListDTO(Pageable pageable);



 Optional<Student> findByUserId(Long id);

@Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.studentYearProfiles p " +
           "LEFT JOIN FETCH p.enrollments e " +
           "LEFT JOIN FETCH p.level " +
           "LEFT JOIN FETCH p.specialty " +
           "LEFT JOIN FETCH p.academicYear " +
           "LEFT JOIN FETCH s.sexe")
    List<Student> findAllWithEnrollments();

    boolean existsByEmail(String email);
    boolean existsByMatricule(String matricule);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
      select s from Student s
      where lower(s.fullName) like lower(concat('%', ?1, '%'))
         or lower(s.matricule) like lower(concat('%', ?1, '%'))
         or lower(s.email) like lower(concat('%', ?1, '%'))
    """)
    List<Student> search(String q);

    @EntityGraph(attributePaths = {
      "studentYearProfiles",
      "studentYearProfiles.enrollments",
      "studentYearProfiles.academicYear",
      "studentYearProfiles.level",
      "studentYearProfiles.specialty"
    })
    @Query("select distinct s from Student s")
    List<Student> findAllWithRelations();

    @Query("""
        select distinct s from Student s
        join s.studentYearProfiles p
        where (:specialtyId is null or p.specialty.id = :specialtyId)
          and (:levelId is null or p.level.id = :levelId)
    """)
    List<Student> findByFilters(Long specialtyId, Long levelId);
}
