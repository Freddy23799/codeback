package codeqr.code.repository;

import codeqr.code.dto.SessionSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveillantSessionRepositoryCustom {
    Page<SessionSummaryDTO> findSessionsBySurveillant(Long surveillantId,
                                                      String teacherName,
                                                      Long specialtyId,
                                                      Long levelId,
                                                      Pageable pageable);
}
