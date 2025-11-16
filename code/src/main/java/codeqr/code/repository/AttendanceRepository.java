package codeqr.code.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import codeqr.code.dto.*;
import codeqr.code.model.Attendance;
import codeqr.code.model.Session;
import codeqr.code.model.StudentYearProfile;

// --- Attendance Repository ---
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>,
        JpaSpecificationExecutor<Attendance>,
        AttendanceRepositoryCustom,
        AttendanceCustomRepository,
        CrudRepository<Attendance, Long> {

    Page<Attendance> findBySessionId(Long sessionId, Pageable pageable);

    Page<Attendance> findBySession_IdAndStudentYearProfileStudentFullNameContainingIgnoreCase(
            Long sessionId,
            String studentName,
            Pageable pageable
    );

    @Query(
            value = "SELECT new codeqr.code.dto.AttendanceDTOS(" +
                    "a.id, a.studentYearProfile.id, s.fullName, a.status, a.scannedAt) " +
                    "FROM Attendance a " +
                    "JOIN a.studentYearProfile sy " +
                    "JOIN sy.student s " +
                    "WHERE a.session.id = :sessionId " +
                    "AND (:studentName IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%',:studentName,'%')))",
            countQuery = "SELECT COUNT(a) " +
                    "FROM Attendance a " +
                    "JOIN a.studentYearProfile sy " +
                    "JOIN sy.student s " +
                    "WHERE a.session.id = :sessionId " +
                    "AND (:studentName IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%',:studentName,'%')))"
    )
    Page<AttendanceDTOS> findBySessionIdWithStudent(
            @Param("sessionId") Long sessionId,
            @Param("studentName") String studentName,
            Pageable pageable
    );

    interface AttendanceCountProjection {
        Long getSessionId();
        String getStatus();
        Long getCount();
    }

    @Query("SELECT a.session.id as sessionId, a.status as status, COUNT(a) as count " +
           "FROM Attendance a WHERE a.session.id IN :sessionIds GROUP BY a.session.id, a.status")
    List<AttendanceCountProjection> countBySessionIdsGrouped(@Param("sessionIds") List<Long> sessionIds);

    @Query("SELECT a.session.id as sessionId, " +
           "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as presentCount, " +
           "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absentCount " +
           "FROM Attendance a WHERE a.session.id IN :ids GROUP BY a.session.id")
    List<AttendanceCountProjection> countsPresentAbsentBySessionIds(@Param("ids") List<Long> ids);

    Optional<Attendance> findByStudentYearProfile_IdAndSession_Id(Long studentYearProfileId, Long sessionId);

    @Query("""
      SELECT a
      FROM Attendance a
      JOIN a.session s
      WHERE a.studentYearProfile.id = :studentYearProfileId
        AND s.startTime BETWEEN :start AND :end
      """)
    List<Attendance> findAttendancesForStudentProfileWithinDateRange(
            @Param("studentYearProfileId") Long studentYearProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Attendance> findByStudentYearProfile_IdAndSessionStartTimeBetween(
            Long studentYearProfileId, LocalDateTime startTime, LocalDateTime endTime
    );

    interface AttendanceCount {
        Long getSessionId();
        Long getPresent();
        Long getAbsent();
    }

    // nativeQuery pour être sûr du comportement CASE ; aliases doivent matcher l'interface
    @Query(value =
       "SELECT a.session_id as sessionId, " +
       "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present, " +
       "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent " +
       "FROM attendance a " +
       "WHERE a.session_id IN :ids " +
       "GROUP BY a.session_id", nativeQuery = true)
    List<AttendanceCount> countAttendanceBySessionIds(@Param("ids") List<Long> ids);

    @Query("SELECT a.session.id, a.status, COUNT(a) " +
           "FROM Attendance a " +
           "WHERE a.session.id IN :ids " +
           "GROUP BY a.session.id, a.status")
    List<Object[]> countGroupedBySessionAndStatus(@Param("ids") List<Long> ids);

    long countByStudentYearProfile_IdIn(List<Long> profileIds);

    long countByStudentYearProfile_IdInAndStatus(List<Long> profileIds, Attendance.Status status);

    long countByStudentYearProfile_IdInAndStatusIn(List<Long> profileIds, List<Attendance.Status> statuses);

    Optional<Attendance> findBySession_IdAndStudentYearProfile_Id(Long sessionId, Long studentYearProfileId);

    List<Attendance> findByStudentYearProfile_Id(Long studentYearProfileId);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.session.id = :sessionId
        AND a.studentYearProfile.id IN (
            SELECT syp.id FROM StudentYearProfile syp
            WHERE syp.student.id = :studentId
        )
    """)
    Optional<Attendance> findBySessionIdAndStudentId(
            @Param("sessionId") Long sessionId,
            @Param("studentId") Long studentId
    );

    // === CORRECTED: use studentYearProfile -> student (no direct a.student) ===
   @Query("""
    SELECT new codeqr.code.dto.AttendanceDTOS(
        a.id,
        sy.id,
        st.fullName,
        a.status,
        a.scannedAt
    )
    FROM Attendance a
    JOIN a.studentYearProfile sy
    JOIN sy.student st
    JOIN a.session s
    WHERE s.id = :sessionId
      AND (:studentName IS NULL OR LOWER(st.fullName) LIKE LOWER(CONCAT('%', :studentName, '%')))
""")
Page<AttendanceDTOS> findBySessionId(
        @Param("sessionId") Long sessionId,
        @Param("studentName") String studentName,
        Pageable pageable
);


    // Rechercher par session id (liste des présences pour afficher)
    List<Attendance> findBySession_Id(Long sessionId);

    // Rechercher attendance pour un profile et une liste de sessions
    List<Attendance> findByStudentYearProfile_IdAndSession_IdIn(Long studentYearProfileId, List<Long> sessionIds);

    @Query("select count(a) from Attendance a where a.studentYearProfile.id in :profileIds")
    long countByStudentYearProfileIds(@Param("profileIds") List<Long> profileIds);

    @Query("select count(a) from Attendance a where a.studentYearProfile.id in :profileIds and a.status = :status")
    long countByStudentYearProfileIdsAndStatus(@Param("profileIds") List<Long> profileIds,
                                               @Param("status") codeqr.code.model.Attendance.Status status);

    // Pour stats : compter presents/absents sur une session
    long countBySession_IdAndStatus(Long sessionId, Attendance.Status status);

    @Query("""
        SELECT a
        FROM Attendance a
        JOIN a.session s
        WHERE a.studentYearProfile.id = :studentYearProfileId
          AND s.startTime BETWEEN :start AND :end
    """)
    List<Attendance> findByStudentAndDateRange(
            @Param("studentYearProfileId") Long studentYearProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT a FROM Attendance a WHERE a.session.id = :sessionId AND a.studentYearProfile.id IN :profileIds")
    List<Attendance> findBySessionIdAndStudentYearProfileIdIn(
        @Param("sessionId") Long sessionId,
        @Param("profileIds") List<Long> profileIds
    );

    @Query("SELECT a FROM Attendance a WHERE a.session = :session")
    List<Attendance> findBySession(@Param("session") Session session);

    Optional<Attendance> findByStudentYearProfileAndSession(StudentYearProfile p, Session s);
}
