


package codeqr.code.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import codeqr.code.model.*;

public interface UserRepository extends JpaRepository<User, Long>,CrudRepository<User, Long>  {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
 @Query("SELECT u FROM User u WHERE u.username = :username")
    User findByUsernames(@Param("username") String username);
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.teacher.id = :teacherId")
    Optional<User> findByRoleAndTeacherId(@Param("role") Role role, @Param("teacherId") Long teacherId);
 // ----- Recherche par token (utilisée pour cleanup) -----
    Optional<User> findByFcmToken(String fcmToken);

    // recherche batch (plus efficace pour nettoyer plusieurs tokens d'un coup)
    List<User> findByFcmTokenIn(Collection<String> tokens);
    List<User> findByRole(Role role);
    List<User> findAllByRole(Role role);
    int countByRole(Role role);

    // 🔹 Récupérer les étudiants filtrés par Spécialité + Niveau + Année (via StudentYearProfile)
   @Query("select u from User u left join fetch u.student s left join fetch s.studentYearProfiles p left join fetch p.specialty left join fetch p.level where u.username = :username")
    Optional<User> findByUsernameWithStudentProfile(@Param("username") String username);
    
 @Modifying
    @Query(value = "UPDATE app_user " +
                   "SET username = :username, " +
                   "    password = CASE WHEN (:password IS NULL OR :password = '') THEN password ELSE :password END " +
                   "WHERE id = :id",
           nativeQuery = true)
    int updateUsernameAndPasswordById(
            @Param("id") Long id,
            @Param("username") String username,
            @Param("password") String password
    );

      boolean existsByUsernameAndIdNot(String username, Long id);
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.student s
        JOIN s.studentYearProfiles syp
        WHERE u.role = codeqr.code.model.Role.ETUDIANT
          AND (:specialtyId IS NULL OR syp.specialty.id = :specialtyId)
          AND (:levelId IS NULL OR syp.level.id = :levelId)
          AND (:academicYearId IS NULL OR syp.academicYear.id = :academicYearId)
    """)
    List<User> findStudentsByYearProfile(@Param("specialtyId") Long specialtyId,
                                         @Param("levelId") Long levelId,
                                         @Param("academicYearId") Long academicYearId);

    // 🔹 Compter les étudiants filtrés par Spécialité + Niveau + Année (via StudentYearProfile)
    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.student s
        JOIN s.studentYearProfiles syp
        WHERE u.role = codeqr.code.model.Role.ETUDIANT
          AND (:specialtyId IS NULL OR syp.specialty.id = :specialtyId)
          AND (:levelId IS NULL OR syp.level.id = :levelId)
          AND (:academicYearId IS NULL OR syp.academicYear.id = :academicYearId)
    """)
    int countStudentsByYearProfile(@Param("specialtyId") Long specialtyId,
                                   @Param("levelId") Long levelId,
                                   @Param("academicYearId") Long academicYearId);






                                   
  
}
