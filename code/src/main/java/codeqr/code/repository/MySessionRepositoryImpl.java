package codeqr.code.repository;

import codeqr.code.dto.SessionDtoss;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySessionRepositoryImpl implements MySessionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Page<SessionDtoss> findSessionsByUsername(String username, String search, Pageable pageable) {
        String searchParam = (search == null || search.trim().isEmpty()) ? null : "%" + search.trim() + "%";

        // -------------------------
        // Requête principale (data)
        // -------------------------
        String dataSql = """
            SELECT
                s.id,
                to_char(s.start_time, 'YYYY-MM-DD') AS date,
                CASE
                    WHEN s.start_time IS NOT NULL AND s.end_time IS NOT NULL
                        THEN to_char(s.start_time,'HH24:MI') || ' - ' || to_char(s.end_time,'HH24:MI')
                    ELSE NULL
                END AS heures,
                c.title AS course_title,
                l.name AS level_name,
                sp.name AS specialty_name,
                r.name AS room_name,
                cp.name AS campus_name,
                COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END),0) AS nb_present,
                COALESCE(SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END),0) AS nb_absent
            FROM session s
                JOIN app_user u ON u.id = s.user_id
                JOIN course c ON c.id = s.course_id
                LEFT JOIN level l ON l.id = s.expected_level_id
                LEFT JOIN specialty sp ON sp.id = s.expected_specialty_id
                LEFT JOIN room r ON r.id = s.room_id
                LEFT JOIN campus cp ON cp.id = s.campus_id
                LEFT JOIN attendance a ON a.session_id = s.id
            WHERE u.username = :username
            """ 
            + (searchParam != null ? " AND (c.title ILIKE :search OR r.name ILIKE :search OR cp.name ILIKE :search) " : "")
            + " GROUP BY s.id, s.start_time, s.end_time, c.title, l.name, sp.name, r.name, cp.name"
            + " ORDER BY s.start_time DESC"
            + " LIMIT :limit OFFSET :offset";

        Query q = em.createNativeQuery(dataSql);
        q.setParameter("username", username);
        if (searchParam != null) {
            q.setParameter("search", searchParam);
        }
        q.setParameter("limit", pageable.getPageSize());
        q.setParameter("offset", pageable.getPageNumber() * pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        List<SessionDtoss> dtos = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = r[0] != null ? ((Number) r[0]).longValue() : null;
            String date = (String) r[1];
            String heures = (String) r[2];
            String courseTitle = (String) r[3];
            String levelName = (String) r[4];
            String specialtyName = (String) r[5];
            String roomName = (String) r[6];
            String campusName = (String) r[7];
            Long nbPresent = r[8] != null ? ((Number) r[8]).longValue() : 0L;
            Long nbAbsent = r[9] != null ? ((Number) r[9]).longValue() : 0L;

            dtos.add(new SessionDtoss(id, date, heures, courseTitle, levelName, specialtyName, roomName, campusName, nbPresent, nbAbsent));
        }

        // -------------------------
        // Requête count
        // -------------------------
        String countSql = """
            SELECT COUNT(*)
            FROM session s
                JOIN app_user u ON u.id = s.user_id
            WHERE u.username = :username
            """ 
            + (searchParam != null
                ? " AND (EXISTS (SELECT 1 FROM course c WHERE c.id = s.course_id AND c.title ILIKE :search) " +
                  "OR EXISTS (SELECT 1 FROM room r WHERE r.id = s.room_id AND r.name ILIKE :search) " +
                  "OR EXISTS (SELECT 1 FROM campus cp WHERE cp.id = s.campus_id AND cp.name ILIKE :search))"
                : "");

        Query cq = em.createNativeQuery(countSql);
        cq.setParameter("username", username);
        if (searchParam != null) {
            cq.setParameter("search", searchParam);
        }

        Number totalNum = (Number) cq.getSingleResult();
        long total = totalNum != null ? totalNum.longValue() : 0L;

        return new PageImpl<>(dtos, pageable, total);
    }
}
