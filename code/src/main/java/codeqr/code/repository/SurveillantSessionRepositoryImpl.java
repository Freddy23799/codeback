package codeqr.code.repository;

import codeqr.code.dto.SessionSummaryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SurveillantSessionRepositoryImpl implements SurveillantSessionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Page<SessionSummaryDTO> findSessionsBySurveillant(Long surveillantId,
                                                             String teacherName,
                                                             Long specialtyId,
                                                             Long levelId,
                                                             Pageable pageable) {

        StringBuilder data = new StringBuilder("""
            SELECT
                s.id,
                s.start_time,  -- on garde en timestamp
                c.title AS course_title,
                l.name AS level_name,
                sp.name AS specialty_name,
                r.name AS room_name,
                cp.name AS campus_name,
                t.full_name AS teacher_full_name,
                COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS present_count,
                COALESCE(COUNT(a.student_year_profile_id), 0) AS total_count,
                s.closed
            FROM session s
            LEFT JOIN course c ON c.id = s.course_id
            LEFT JOIN level l ON l.id = s.expected_level_id
            LEFT JOIN specialty sp ON sp.id = s.expected_specialty_id
            LEFT JOIN room r ON r.id = s.room_id
            LEFT JOIN campus cp ON cp.id = s.campus_id
            LEFT JOIN attendance a ON a.session_id = s.id
            LEFT JOIN "user" u ON u.id = s.user_id
            LEFT JOIN teacher t ON t.id = u.teacher_id
            WHERE s.surveillant_id = :surveillantId
        """);

        if (teacherName != null && !teacherName.trim().isEmpty()) {
            data.append(" AND t.full_name ILIKE :teacherName ");
        }
        if (specialtyId != null) {
            data.append(" AND s.expected_specialty_id = :specialtyId ");
        }
        if (levelId != null) {
            data.append(" AND s.expected_level_id = :levelId ");
        }

        data.append("""
            GROUP BY s.id, s.start_time, c.title, l.name, sp.name, r.name, cp.name, t.full_name, s.closed
            ORDER BY s.start_time DESC
            LIMIT :limit OFFSET :offset
        """);

        Query q = em.createNativeQuery(data.toString());
        q.setParameter("surveillantId", surveillantId);
        if (teacherName != null && !teacherName.trim().isEmpty()) q.setParameter("teacherName", "%" + teacherName.trim() + "%");
        if (specialtyId != null) q.setParameter("specialtyId", specialtyId);
        if (levelId != null) q.setParameter("levelId", levelId);
        q.setParameter("limit", pageable.getPageSize());
        q.setParameter("offset", (long) pageable.getPageNumber() * pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<SessionSummaryDTO> content = new ArrayList<>(rows.size());

        for (Object[] r : rows) {
            Long sessionId = r[0] != null ? ((Number) r[0]).longValue() : null;
            LocalDateTime startTime = r[1] != null ? ((java.sql.Timestamp) r[1]).toLocalDateTime() : null;
            String courseTitle = r[2] != null ? r[2].toString() : null;
            String levelName = r[3] != null ? r[3].toString() : null;
            String specialtyName = r[4] != null ? r[4].toString() : null;
            String roomName = r[5] != null ? r[5].toString() : null;
            String campusName = r[6] != null ? r[6].toString() : null;
            String teacherFullName = r[7] != null ? r[7].toString() : null;
            Long presentCount = r[8] != null ? ((Number) r[8]).longValue() : 0L;
            Long totalCount = r[9] != null ? ((Number) r[9]).longValue() : 0L;
            Boolean closed = r[10] != null ? (Boolean) r[10] : Boolean.FALSE;

            content.add(new SessionSummaryDTO(
                    sessionId,
                    startTime,
                    courseTitle,
                    levelName,
                    specialtyName,
                    roomName,
                    campusName,
                    teacherFullName,
                    presentCount,
                    totalCount,
                    closed
            ));
        }

        // Count query
        StringBuilder count = new StringBuilder("""
            SELECT COUNT(*)
            FROM session s
            LEFT JOIN "user" u ON u.id = s.user_id
            LEFT JOIN teacher t ON t.id = u.teacher_id
            WHERE s.surveillant_id = :surveillantId
        """);
        if (teacherName != null && !teacherName.trim().isEmpty()) count.append(" AND t.full_name ILIKE :teacherName ");
        if (specialtyId != null) count.append(" AND s.expected_specialty_id = :specialtyId ");
        if (levelId != null) count.append(" AND s.expected_level_id = :levelId ");

        Query cq = em.createNativeQuery(count.toString());
        cq.setParameter("surveillantId", surveillantId);
        if (teacherName != null && !teacherName.trim().isEmpty()) cq.setParameter("teacherName", "%" + teacherName.trim() + "%");
        if (specialtyId != null) cq.setParameter("specialtyId", specialtyId);
        if (levelId != null) cq.setParameter("levelId", levelId);

        Number totalNum = (Number) cq.getSingleResult();
        long total = totalNum != null ? totalNum.longValue() : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
