
package codeqr.code.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import codeqr.code.model.Attendance;
import codeqr.code.projection.AbsenceReportProjection;

@Repository
public interface AbsenceReportRepository extends JpaRepository<Attendance, Long> {

    /**
     * Native SQL (MySQL) — calcule la somme des minutes d'intersection entre
     * chaque session et la fenêtre [:start, :end], convertit en heures (décimal),
     * filtre attendances ABSENT/PENDING et profils actifs pour academicYear/level/specialty.
     *
     * IMPORTANT : adapte les noms de tables / colonnes si nécessaire.
     */
    @Query(value = """
        SELECT
            st.id AS studentId,
            st.matricule AS matricule,
            st.full_name AS fullName,
            COALESCE(sx.name, 'N/A') AS sexe,
            SUM(
                CASE
                    WHEN LEAST(s.end_time, :end) > GREATEST(s.start_time, :start)
                    THEN EXTRACT(EPOCH FROM (LEAST(s.end_time, :end) - GREATEST(s.start_time, :start))) / 3600.0
                    ELSE 0
                END
            ) AS absentHours,
            COUNT(a.id) AS sessionsCount
        FROM attendance a
        JOIN session s ON s.id = a.session_id
        JOIN student_year_profile p ON p.id = a.student_year_profile_id
        JOIN student st ON st.id = p.student_id
        LEFT JOIN sexe sx ON sx.id = st.sexe_id
        WHERE p.academic_year_id = :academicYearId
          AND p.level_id = :levelId
          AND p.specialty_id = :specialtyId
          AND p.active = TRUE
          AND a.status IN ('ABSENT', 'PENDING')
          AND s.end_time > :start
          AND s.start_time < :end
        GROUP BY st.id, st.matricule, st.full_name, sx.name
        ORDER BY absentHours DESC
        """, nativeQuery = true)
List<AbsenceReportProjection> findAbsenceReportByFilters(
        @Param("academicYearId") Long academicYearId,
        @Param("levelId") Long levelId,
        @Param("specialtyId") Long specialtyId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
);


}
