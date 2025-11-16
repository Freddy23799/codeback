// StatsService.java
package codeqr.code.service;

import codeqr.code.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final EntityManager em;

    /**
     * Build course stats for a teacher and the currently selected filters (specialty/level/course/year).
     */
    public CourseStatsDto computeCourseStats(Long teacherId, Long specialtyId, Long levelId, Long academicYearId, Long courseId) {
        System.out.println("=== computeCourseStats START ===");
        System.out.println("Input teacherId: " + teacherId);
        System.out.println("Filters -> specialtyId: " + specialtyId + ", levelId: " + levelId + ", academicYearId: " + academicYearId + ", courseId: " + courseId);

        // --- Étape 1 : récupérer le user_id correspondant au teacherId ---
        Long userId = null;
        try {
            userId = (Long) em.createNativeQuery("SELECT user_id FROM teacher WHERE id = :tid")
                    .setParameter("tid", teacherId)
                    .getSingleResult();
            System.out.println("Found userId for teacherId " + teacherId + ": " + userId);
        } catch (Exception e) {
            System.out.println("ERROR: Impossible de trouver userId pour teacherId " + teacherId);
            e.printStackTrace();
            return null;
        }

        // --- Étape 2 : construire le WHERE pour filtrer les sessions ---
        String baseWhere = "WHERE s.user_id = :userId ";
        if (specialtyId != null) baseWhere += "AND s.expected_specialty_id = :specialtyId ";
        if (levelId != null) baseWhere += "AND s.expected_level_id = :levelId ";
        if (academicYearId != null) baseWhere += "AND s.academic_year_id = :academicYearId ";
        if (courseId != null) baseWhere += "AND s.course_id = :courseId ";

        System.out.println("Constructed baseWhere: " + baseWhere);

        // --- 1) totalSessions et période (min/max startTime) ---
        try {
            String q1 = "SELECT COUNT(*) as cnt, MIN(s.start_time) as min_start, MAX(s.start_time) as max_start FROM session s " + baseWhere;
            Query query1 = em.createNativeQuery(q1);
            query1.setParameter("userId", userId);
            if (specialtyId != null) query1.setParameter("specialtyId", specialtyId);
            if (levelId != null) query1.setParameter("levelId", levelId);
            if (academicYearId != null) query1.setParameter("academicYearId", academicYearId);
            if (courseId != null) query1.setParameter("courseId", courseId);

            Object[] q1res = (Object[]) query1.getSingleResult();
            Integer totalSessions = ((Number) q1res[0]).intValue();
            java.sql.Timestamp minTs = (java.sql.Timestamp) q1res[1];
            java.sql.Timestamp maxTs = (java.sql.Timestamp) q1res[2];
            LocalDateTime periodStart = minTs != null ? minTs.toLocalDateTime() : null;
            LocalDateTime periodEnd = maxTs != null ? maxTs.toLocalDateTime() : null;

            System.out.println("Total sessions: " + totalSessions + ", periodStart: " + periodStart + ", periodEnd: " + periodEnd);

            // --- 2) sessions par campus ---
            String q2 = "SELECT cp.name AS campusName, COUNT(*) as cnt FROM session s " +
                        "JOIN campus cp ON cp.id = s.campus_id " + baseWhere +
                        "GROUP BY cp.name ORDER BY cnt DESC";
            Query query2 = em.createNativeQuery(q2);
            query2.setParameter("userId", userId);
            if (specialtyId != null) query2.setParameter("specialtyId", specialtyId);
            if (levelId != null) query2.setParameter("levelId", levelId);
            if (academicYearId != null) query2.setParameter("academicYearId", academicYearId);
            if (courseId != null) query2.setParameter("courseId", courseId);

            List<Object[]> rows2 = query2.getResultList();
            List<CampusCountDto> campusCounts = rows2.stream()
                    .map(r -> new CampusCountDto((String) r[0], ((Number) r[1]).longValue()))
                    .collect(Collectors.toList());
            System.out.println("Campus counts retrieved: " + campusCounts.size());

            // --- 3) échantillon de sessions (latest 10) ---
            String q3 = "SELECT s.id, c.title, s.start_time, cp.name, lvl.name, sp.name, r.name FROM session s " +
                        "JOIN course c ON c.id = s.course_id " +
                        "JOIN campus cp ON cp.id = s.campus_id " +
                        "JOIN level lvl ON lvl.id = s.expected_level_id " +
                        "JOIN specialty sp ON sp.id = s.expected_specialty_id " +
                        "LEFT JOIN room r ON r.id = s.room_id " + baseWhere +
                        " ORDER BY s.start_time DESC LIMIT 30";
            Query query3 = em.createNativeQuery(q3);
            query3.setParameter("userId", userId);
            if (specialtyId != null) query3.setParameter("specialtyId", specialtyId);
            if (levelId != null) query3.setParameter("levelId", levelId);
            if (academicYearId != null) query3.setParameter("academicYearId", academicYearId);
            if (courseId != null) query3.setParameter("courseId", courseId);

            List<Object[]> rows3 = query3.getResultList();
            List<SessionSampleDto> sample = rows3.stream().map(r -> {
                Long id = ((Number) r[0]).longValue();
                String title = (String) r[1];
                java.sql.Timestamp t = (java.sql.Timestamp) r[2];
                LocalDateTime start = t != null ? t.toLocalDateTime() : null;
                return new SessionSampleDto(id, title, start, (String) r[3], (String) r[4], (String) r[5], (String) r[6]);
            }).collect(Collectors.toList());
            System.out.println("Sample sessions retrieved: " + sample.size());

            // --- 4) récupérer le nom du professeur ---
            String teacherName = (String) em.createNativeQuery("SELECT full_name FROM teacher t WHERE t.id = :tid")
                                            .setParameter("tid", teacherId)
                                            .getSingleResult();
            System.out.println("Teacher name: " + teacherName);

            // --- 5) construire le DTO ---
            CourseStatsDto dto = new CourseStatsDto();
            dto.setTeacherId(teacherId);
            dto.setTeacherName(teacherName);
            dto.setTotalSessions(totalSessions);
            dto.setPeriodStart(periodStart);
            dto.setPeriodEnd(periodEnd);
            dto.setSessionsByCampus(campusCounts);
            dto.setSessionsSample(sample);

            System.out.println("=== computeCourseStats END ===");
            return dto;

        } catch (Exception e) {
            System.out.println("ERROR during computeCourseStats queries");
            e.printStackTrace();
            return null;
        }
    }
}
