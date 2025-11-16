package codeqr.code.repository;

import codeqr.code.dto.AttendanceHistoryFilter;
import codeqr.code.dto.AttendanceHistoryRowDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class AttendanceRepositoryImpl implements AttendanceRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<AttendanceHistoryRowDTO> findHistoryForStudent(Long studentId, AttendanceHistoryFilter f, Pageable pageable) {

        String selectClause = """
            select new codeqr.code.repository.RowProjection(
                a.id,
                c.title,
                s.startTime,
                cp.name,
                r.name,
                t.fullName,
                a.status
            )
        """;

        String fromClause = """
            from Attendance a
            join a.studentYearProfile syp     
            join syp.student st              
            join a.session s                
            join s.course c
            left join s.campus cp
            left join s.room r
            left join s.user u              
            left join u.teacher t            
            where st.id = :studentId
        """;

        StringBuilder where = new StringBuilder(fromClause);
        Map<String,Object> params = new HashMap<>();
        params.put("studentId", studentId);

        // --- Gestion période / dates ---
        LocalDateTime start = null, end = null;
        if (f != null) {
            if ("this_week".equalsIgnoreCase(f.period)) {
                var today = java.time.LocalDate.now();
                var monday = today.with(java.time.DayOfWeek.MONDAY);
                start = monday.atStartOfDay();
                end   = monday.plusDays(7).atStartOfDay().minusNanos(1);
            } else if ("this_month".equalsIgnoreCase(f.period)) {
                var today = java.time.LocalDate.now();
                var first = today.withDayOfMonth(1);
                var last  = first.plusMonths(1).minusDays(1);
                start = first.atStartOfDay();
                end   = last.atTime(23,59,59,999_000_000);
            } else if ("custom".equalsIgnoreCase(f.period)) {
                if (f.from != null) start = f.from.atStartOfDay();
                if (f.to   != null) end   = f.to.atTime(23,59,59,999_000_000);
            }
        }
        if (start != null) { where.append(" and s.startTime >= :start "); params.put("start", start); }
        if (end != null)   { where.append(" and s.startTime <= :end ");   params.put("end", end); }

        // --- Filtres supplémentaires ---
        if (f != null) {
            if (f.courseId != null) { where.append(" and c.id = :courseId "); params.put("courseId", f.courseId); }
            if (f.campusId != null) { where.append(" and cp.id = :campusId "); params.put("campusId", f.campusId); }
            if (f.roomId   != null) { where.append(" and r.id  = :roomId ");   params.put("roomId",   f.roomId); }
            if (f.status   != null && !f.status.isBlank()) {
                where.append(" and a.status = :status ");
                params.put("status", Enum.valueOf(codeqr.code.model.Attendance.Status.class, f.status));
            }
            if (f.q != null && !f.q.isBlank()) {
                where.append("""
                    and (
                         lower(c.title) like :q
                      or lower(t.fullName) like :q
                      or lower(cp.name) like :q
                      or lower(r.name) like :q
                    )
                """);
                params.put("q", "%" + f.q.toLowerCase().trim() + "%");
            }
        }

        // --- Order By ---
        String orderBy = orderClauseFor(pageable);

        // --- Query data ---
        String dataQueryStr = selectClause + where + orderBy;
        TypedQuery<RowProjection> query = em.createQuery(dataQueryStr, RowProjection.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<RowProjection> rows = query.getResultList();

        // --- Count query ---
        String countQueryStr = "select count(a.id) " + where;
        TypedQuery<Long> countQuery = em.createQuery(countQueryStr, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        // --- Mapping final ---
        List<AttendanceHistoryRowDTO> result = rows.stream().map(RowProjection::toDto).toList();
        return new PageImpl<>(result, pageable, total);
    }

    private String orderClauseFor(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return " order by s.startTime desc ";
        }
        List<String> parts = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String prop = switch (order.getProperty()) {
                case "sessionStart", "dayOfWeek" -> "s.startTime";
                case "courseTitle" -> "c.title";
                case "campusName"  -> "cp.name";
                case "roomName"    -> "r.name";
                case "teacherName" -> "t.fullName";
                case "status"      -> "a.status";
                default            -> "s.startTime";
            };
            parts.add(prop + " " + (order.isAscending() ? "asc" : "desc"));
        });
        return " order by " + String.join(", ", parts) + " ";
    }
}
