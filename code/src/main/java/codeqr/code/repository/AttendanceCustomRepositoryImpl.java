

package codeqr.code.repository;

import codeqr.code.model.Attendance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AttendanceCustomRepositoryImpl implements AttendanceCustomRepository {

    @PersistenceContext
    private EntityManager em;

   @Override
public Map<String, Long> countAttendanceBySession(Long sessionId) {
    String jpql = "SELECT a.status, COUNT(a) " +
                  "FROM Attendance a " +
                  "WHERE a.session.id = :sessionId " +
                  "GROUP BY a.status";

    List<Object[]> results = em.createQuery(jpql, Object[].class)
                               .setParameter("sessionId", sessionId)
                               .getResultList();

    Map<String, Long> counts = new HashMap<>();
    counts.put("PRESENT", 0L);
    counts.put("ABSENT", 0L);

    for (Object[] row : results) {
        Attendance.Status statusEnum = (Attendance.Status) row[0];
        Long count = (Long) row[1];
        counts.put(statusEnum.toString(), count);
    }

    return counts;
}

}
