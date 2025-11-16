package codeqr.code.service;

import codeqr.code.dto.AttendanceHistoryFilter;
import codeqr.code.dto.AttendanceHistoryRowDTO;
import codeqr.code.repository.AttendanceRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class StudentAttendanceService {

    private final AttendanceRepository attendanceRepository;

    public StudentAttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Page<AttendanceHistoryRowDTO> history(Long studentId, AttendanceHistoryFilter filter, Pageable pageable) {
        return attendanceRepository.findHistoryForStudent(studentId, filter, pageable);
    }
}
