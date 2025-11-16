package codeqr.code.service;

import codeqr.code.dto.PageResponse;
import codeqr.code.dto.PageResponses;
import codeqr.code.dto.AttendanceDTOS;
import codeqr.code.dto.SessionSummaryDTO;
import codeqr.code.repository.AttendanceRepository;
import codeqr.code.repository.SurveillantSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveillantServices {

    private final SurveillantSessionRepository repository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Sessions par surveillant (pageable) — cacheable
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "surveillant:sessions",
        key = "T(String).format('%d:%d:%d:%s:%s:%s', " +
              "#surveillantId, " +
              "#pageable.pageNumber, " +
              "#pageable.pageSize, " +
              "(#teacherName == null ? '' : #teacherName), " +
              "(#specialtyId == null ? '' : #specialtyId.toString()), " +
              "(#levelId == null ? '' : #levelId.toString()))")
    public PageResponse<SessionSummaryDTO> getSessionsBySurveillant(Long surveillantId,
                                                                    String teacherName,
                                                                    Long specialtyId,
                                                                    Long levelId,
                                                                    Pageable pageable) {
        Page<SessionSummaryDTO> page =
                repository.findSessionsBySurveillant(surveillantId, teacherName, specialtyId, levelId, pageable);

        return new PageResponse<>(
                page.getContent(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Attendances for a session (pageable) — cacheable
     *
     * Note: we return DTOs (AttendanceDTOS) wrapped in PageResponsez to avoid serializing JPA entities.
     */
    @Cacheable(
        value = "attendancesBySession",
        key = "#sessionId + '-' + (#studentName == null ? '' : #studentName) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public PageResponses<AttendanceDTOS> getAttendancesForSession(Long sessionId,
                                                                 String studentName,
                                                                 Pageable pageable) {
        Page<AttendanceDTOS> page = attendanceRepository.findBySessionId(sessionId, studentName, pageable);

        List<AttendanceDTOS> content = page.getContent();

        return new PageResponses<>(
                content,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // Exemple d'invalidation simple (décommenter / utiliser lors d'opérations create/update/delete)
    // @CacheEvict(value = {"surveillant:sessions", "attendancesBySession"}, allEntries = true)
    // public void evictAllCaches() { /* empty - annotation handle eviction */ }
}
