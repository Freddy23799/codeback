package codeqr.code.repository;

import codeqr.code.dto.AttendanceHistoryFilter;
import codeqr.code.dto.AttendanceHistoryRowDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceRepositoryCustom {
    Page<AttendanceHistoryRowDTO> findHistoryForStudent(Long studentId, AttendanceHistoryFilter filter, Pageable pageable);
}
