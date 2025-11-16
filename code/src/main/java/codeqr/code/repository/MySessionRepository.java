package codeqr.code.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import codeqr.code.dto.*;
import codeqr.code.model.*;

@Repository
public interface MySessionRepository extends JpaRepository<Session, Long>,
        SessionCustomRepository,
        JpaSpecificationExecutor<Session>,
        SessionRepositoryCustoms,
        MySessionRepositoryCustom,
        PagingAndSortingRepository<Session, Long> {

    // ✅ Requête principale pour récupérer les sessions par surveillant
   
    @Query(value = """
    SELECT s.id AS sessionId,
           s.start_time AS startTime,
           c.title AS courseTitle,
           l.name AS levelName,
           sp.name AS specialtyName,
           r.name AS roomName,
           cp.name AS campusName,
           t.full_name AS teacherFullName,
           SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
           COUNT(a.id) AS totalCount,
           s.closed AS closed
    FROM session s
    JOIN course c ON s.course_id = c.id
    JOIN room r ON s.room_id = r.id
    JOIN campus cp ON s.campus_id = cp.id
    JOIN specialty sp ON s.expected_specialty_id = sp.id
    JOIN level l ON s.expected_level_id = l.id
    JOIN app_user u ON s.user_id = u.id
    JOIN teacher t ON t.user_id = u.id
    LEFT JOIN attendance a ON a.session_id = s.id
    WHERE s.surveillant_id = :surveillantId
      AND (:teacherName IS NULL OR LOWER(t.full_name) LIKE LOWER(CONCAT('%', :teacherName, '%')))
      AND (:specialtyId IS NULL OR sp.id = :specialtyId)
      AND (:levelId IS NULL OR l.id = :levelId)
    GROUP BY s.id, s.start_time, c.title, l.name, sp.name, r.name, cp.name, t.full_name, s.closed
    ORDER BY s.start_time DESC
    """,
    countQuery = """
    SELECT COUNT(DISTINCT s.id)
    FROM session s
    JOIN app_user u ON s.user_id = u.id
    JOIN teacher t ON t.user_id = u.id
    WHERE s.surveillant_id = :surveillantId
      AND (:teacherName IS NULL OR LOWER(t.full_name) LIKE LOWER(CONCAT('%', :teacherName, '%')))
      AND (:specialtyId IS NULL OR s.expected_specialty_id = :specialtyId)
      AND (:levelId IS NULL OR s.expected_level_id = :levelId)
    """,
    nativeQuery = true
)
Page<Object[]> findSessionsForSurveillantNative(
        @Param("surveillantId") Long surveillantId,
        @Param("teacherName") String teacherName,
        @Param("specialtyId") Long specialtyId,
        @Param("levelId") Long levelId,
        Pageable pageable
);

    // ✅ reste de tes méthodes (inchangées)
    @Query("select distinct s from Session s " +
            "left join fetch s.course c " +
            "left join fetch s.room r " +
            "left join fetch s.campus cp " +
            "left join fetch s.expectedLevel el " +
            "left join fetch s.expectedSpecialty sp " +
            "left join fetch s.user u " +
            "left join fetch u.teacher t " +
            "where s.id in :ids")
    List<Session> findAllByIdInWithFetch(@Param("ids") List<Long> ids);

    List<Session> findByAcademicYearId(Long academicYearId);
    List<Session> findByCampusId(Long campusId);
    List<Session> findByUserId(Long userId);
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Session> findByCourseId(Long courseId);

    List<Session> findByAcademicYear_IdAndExpectedLevel_IdAndExpectedSpecialty_Id(
            Long academicYearId, Long levelId, Long specialtyId);

    List<Session> findByExpectedLevelAndExpectedSpecialtyAndAcademicYear(Level level, Specialty specialty, AcademicYear academicYear);

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

    @Query("SELECT COUNT(s) FROM Session s WHERE s.user.id = :userId")
    long countSessionsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT new codeqr.code.dto.PresenceSummary(
                SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END)
            )
            FROM Session s
            JOIN s.attendances a
            WHERE s.user.id = :userId
            """)
    PresenceSummary sumPresenceAndAbsence(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(a)
            FROM Session s
            JOIN s.attendances a
            WHERE s.user.id = :userId
              AND (a.status = 'PRESENT' OR a.status = 'ABSENT')
            """)
    Long countTotalStudents(@Param("userId") Long userId);

    @Query("""
            SELECT s.course.title,
                   SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END)
            FROM Session s
            JOIN s.attendances a
            WHERE s.user.id = :userId
            GROUP BY s.course.title
            """)
    List<Object[]> groupByCourse(@Param("userId") Long userId);

    @Query("SELECT s FROM Session s " +
            "LEFT JOIN FETCH s.course " +
            "LEFT JOIN FETCH s.campus " +
            "LEFT JOIN FETCH s.room " +
            "LEFT JOIN FETCH s.teacherYearProfile t " +
            "LEFT JOIN FETCH t.teacher ")
    List<Session> findAllWithDetails();

    @Query("SELECT s FROM Session s WHERE s.id = :id")
    Optional<Session> findSessionShallow(@Param("id") Long id);

    @Query("""
            SELECT new codeqr.code.dto.SessionLightDO(
                s.id,
                CAST(FUNCTION('DATE_FORMAT', s.startTime, '%Y-%m-%d') AS string),
                CONCAT(
                    CAST(FUNCTION('DATE_FORMAT', s.startTime, '%H:%i') AS string),
                    ' - ',
                    CAST(FUNCTION('DATE_FORMAT', s.endTime, '%H:%i') AS string)
                ),
                s.course.title,
                s.expectedLevel.name,
                s.expectedSpecialty.name,
                s.room.name,
                s.campus.name,
                s.user.teacher.fullName,
                CAST((SELECT COUNT(a) FROM Attendance a WHERE a.session = s AND a.status = 'PRESENT') AS long),
                CAST((SELECT COUNT(a) FROM Attendance a WHERE a.session = s AND a.status = 'ABSENT') AS long)
            )
            FROM Session s
            WHERE (:search IS NULL OR LOWER(s.course.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(s.room.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(s.campus.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:date IS NULL OR FUNCTION('DATE', s.startTime) = :date)
            """)
    Page<SessionLightDO> searchSessions(
            @Param("search") String search,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    @Query("""
            SELECT s FROM Session s
            JOIN FETCH s.course c
            JOIN FETCH s.expectedLevel l
            JOIN FETCH s.expectedSpecialty sp
            JOIN FETCH s.room r
            JOIN FETCH s.campus cm
            WHERE s.user.id = :userId
            AND (
                c.title LIKE %:search%
                OR l.name LIKE %:search%
                OR sp.name LIKE %:search%
                OR r.name LIKE %:search%
                OR cm.name LIKE %:search%
            )
            """)
    Page<Session> findByUserWithSearch(Long userId, String search, Pageable pageable);
}
