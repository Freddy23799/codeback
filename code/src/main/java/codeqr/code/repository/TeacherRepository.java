

package codeqr.code.repository;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import codeqr.code.model.Role;
import codeqr.code.model.Student;
import codeqr.code.model.Teacher;
import codeqr.code.model.User;
// --- Teacher Repository ---
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long>, TeacherRepositoryCustom {
List<Teacher> findAllByIdIn(Collection<Long> ids);

 

  @Query("SELECT t FROM Teacher t WHERE t.user.id IN :userIds")
    List<Teacher> findByUserIds(@Param("userIds") List<Long> userIds);

    // helper default method (Java 8+) to produce a map userId->fullName
    default Map<Long, String> mapUserIdToFullName(List<Long> userIds) {
        return findByUserIds(userIds).stream()
            .collect(Collectors.toMap(t -> t.getUser().getId(), Teacher::getFullName));
    }



 @Query("SELECT t FROM Teacher t " +
           "JOIN FETCH t.sexe sx " +
           "JOIN FETCH t.user u " +
           "WHERE t.user.id = :userId")
    Optional<Teacher> findByUserIdWithSexe(@Param("userId") Long userId);
    Optional<Teacher> findByEmail(String email);

  Optional<Teacher> findById(Long id);

   @Query("SELECT t.id AS id, t.fullName AS fullName FROM Teacher t")
    List<Object[]> findAllIdAndFullName();
    
     Optional<Teacher> findByEmailIgnoreCase(String email);
     
   Optional<Teacher> findByUserUsername(String username);
    // Optional<Teacher> findByUserUsername(String username);     
            // si déjà présent OK
    Optional<Teacher> findByUserUsernameIgnoreCase(String username); 
        // à ajouter
    @Query("SELECT t FROM Teacher t JOIN t.user u WHERE u.id = :userId AND u.role = :role")
Optional<Teacher> findByUserIdAndUserRole(@Param("userId") Long userId, @Param("role") Role role);
  
                       // si déjà présent OK
  // @Query("SELECT t FROM Teacher t WHERE t.email = :login OR t.name = :login")
  //   Optional<Teacher> findByEmailOrName(@Param("login") String login);
 
  
 Optional<Teacher> findByUser(User user);


 Optional<Teacher> findByUserId(Long id);

 Page<Teacher> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
    String fullName, String email, String username, String matricule, Pageable pageable
);

  //  @Query("SELECT t FROM Teacher t " +
  //          "LEFT JOIN FETCH t.teacherYearProfiles typ " +
  //          "LEFT JOIN FETCH typ.specialtyProfiles sp " +
  //          "LEFT JOIN FETCH sp.levels l " +
  //          "LEFT JOIN FETCH l.sessions s " +
  //          "LEFT JOIN FETCH s.attendances a " +
  //          "LEFT JOIN FETCH a.studentYearProfile syp " +
  //          "LEFT JOIN FETCH syp.student st " +
  //          "LEFT JOIN FETCH s.course " +
  //          "LEFT JOIN FETCH s.campus " +
  //          "LEFT JOIN FETCH s.room " +
  //          "LEFT JOIN FETCH t.sexe")
  //   List<Teacher> findAllWithFullHierarchy();
  Page<Teacher> findByFullNameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(String fullName, String matricule, Pageable pageable);





    @Query(value = """
        SELECT t.id AS teacher_id, t.full_name, t.email, t.matricule,
               COALESCE(sc.sessions_count,0) AS sessions_count
        FROM teacher t
        LEFT JOIN (
          SELECT s.user_id AS user_id, COUNT(*) AS sessions_count FROM session s GROUP BY s.user_id
        ) sc ON sc.user_id = t.user_id
        WHERE (:q IS NULL OR (
            to_tsvector('french', coalesce(t.full_name,'') || ' ' || coalesce(t.matricule,'') || ' ' || coalesce(t.email,'')) @@ plainto_tsquery('french', :q)
        ))
        /* filters on specialty/level/year/course are applied at sessions level: we keep them out of this simple query or implement via joins if needed */
        AND ( :cursor IS NULL OR t.id < :cursor )
        ORDER BY t.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchTeachersNative(@Param("q") String q,
                                        @Param("cursor") Long cursor,
                                        @Param("limit") int limit);
}