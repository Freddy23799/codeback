
package codeqr.code.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import codeqr.code.dto.SessionDtoss;
import codeqr.code.dto.SessionLightDTO;
import codeqr.code.model.Session;
import codeqr.code.model.User;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, SessionCustomRepository, JpaSpecificationExecutor<Session> ,SessionRepositoryCustoms ,PagingAndSortingRepository<Session, Long> {

    List<Session> findByTeacherYearProfileId(Long teacherYearProfileId);

    List<Session> findByAcademicYearId(Long academicYearId);

    List<Session> findByCampusId(Long campusId);

    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Session> findByCourseId(Long courseId);

 




 @Query("""
        SELECT COUNT(s) 
        FROM Session s 
        WHERE s.user.id = :userId
    """)
    long countSessionsByUserId(@Param("userId") Long userId);

  
 @Query(value = """
        SELECT s.id, c.title AS course_title, s.start_time, cp.name AS campus_name,
               lvl.name AS level_name, sp.name AS specialty_name, r.name AS room_name
        FROM session s
        JOIN course c ON c.id = s.course_id
        JOIN campus cp ON cp.id = s.campus_id
        JOIN level lvl ON lvl.id = s.expectedLevel_id
        JOIN specialty sp ON sp.id = s.expectedSpecialty_id
        LEFT JOIN room r ON r.id = s.room_id
        WHERE s.user_id = :teacherUserId
          AND (:cursor IS NULL OR s.id < :cursor)
        ORDER BY s.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSessionsByTeacherKeyset(@Param("teacherUserId") Long teacherUserId,
                                               @Param("cursor") Long cursor,
                                               @Param("limit") int limit);




 @Query("SELECT s FROM Session s WHERE s.user.teacher.id = :teacherId")
    List<Session> findByTeacherId(@Param("teacherId") Long teacherId);
    List<Session> findByAcademicYear_IdAndExpectedLevel_IdAndExpectedSpecialty_Id(
            Long academicYearId, Long levelId, Long specialtyId);

    @Query("""
        SELECT s
        FROM Session s
        LEFT JOIN FETCH s.course
        LEFT JOIN FETCH s.campus
        LEFT JOIN FETCH s.room
        LEFT JOIN FETCH s.teacherYearProfile
        WHERE s.expectedLevel.id = :levelId
          AND s.expectedSpecialty.id = :specialtyId
          AND s.academicYear.id = :academicYearId
    """)
    List<Session> findByExpectedLevelIdAndExpectedSpecialtyIdAndAcademicYearId(
            @Param("levelId") Long levelId,
            @Param("specialtyId") Long specialtyId,
            @Param("academicYearId") Long academicYearId
    );

    @Query("SELECT s FROM Session s " +
           "LEFT JOIN FETCH s.course " +
           "LEFT JOIN FETCH s.campus " +
           "LEFT JOIN FETCH s.room " +
           "LEFT JOIN FETCH s.teacherYearProfile t " +
           "LEFT JOIN FETCH t.teacher " +
           "LEFT JOIN FETCH s.expectedLevel " +
           "LEFT JOIN FETCH s.expectedSpecialty " +
           "WHERE s.expectedLevel.id = :levelId " +
           "AND s.expectedSpecialty.id = :specialtyId " +
           "AND s.academicYear.id = :academicYearId " +
           "ORDER BY s.startTime DESC")
    List<Session> findByExpectedLevelAndSpecialtyAndAcademicYearWithDetails(
        @Param("levelId") Long levelId,
        @Param("specialtyId") Long specialtyId,
        @Param("academicYearId") Long academicYearId
    );

    List<Session> findByUserAndAcademicYear_LabelAndExpectedSpecialty_NameAndExpectedLevel_NameAndStartTimeBetween(
        User user,
        String academicYearLabel,
        String specialtyName,
        String levelName,
        LocalDateTime start,
        LocalDateTime end
    );

    List<Session> findByUser(User user);

    Optional<Session> findByQrToken(String qrToken);

    @Query("SELECT s FROM Session s " +
           "LEFT JOIN FETCH s.course " +
           "LEFT JOIN FETCH s.campus " +
           "LEFT JOIN FETCH s.room " +
           "LEFT JOIN FETCH s.teacherYearProfile t " +
           "LEFT JOIN FETCH t.teacher " +
           "WHERE s.id IN :ids")
    List<Session> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT s FROM Session s " +
           "LEFT JOIN FETCH s.course " +
           "LEFT JOIN FETCH s.campus " +
           "LEFT JOIN FETCH s.room " +
           "LEFT JOIN FETCH s.teacherYearProfile t " +
           "LEFT JOIN FETCH t.teacher ")
    List<Session> findAllWithDetails();

    List<Session> findByExpiryTimeBeforeAndNotifiedFalse(LocalDateTime now);

    // ✅ Corrigé : jointures via navigation d’association pour éviter le mot réservé "user"
    @Query("select new codeqr.code.dto.SessionLightDTO(" +
           "s.id, " +
           "a.studentYearProfile.id, " +
           "s.course.title, " +
           "s.campus.name, " +
           "s.room.name, " +
           "t.fullName, " +
           "s.startTime, " +
           "s.endTime, " +
           "a.status) " +
           "from Session s " +
           "join s.attendances a " +
           "join s.user u " +
           "join u.teacher t " +
           "where a.studentYearProfile.student.id in :studentIds")
    List<SessionLightDTO> findSessionsByStudentIds(@Param("studentIds") List<Long> studentIds);

    @Query("SELECT s FROM Session s WHERE s.id = :sessionId")
    Optional<Session> findSessionById(@Param("sessionId") Long sessionId);











    
}
