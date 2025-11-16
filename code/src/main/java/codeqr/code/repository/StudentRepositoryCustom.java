package codeqr.code.repository;

import codeqr.code.dto.StudentListDTO;
import codeqr.code.dto.EnrollmentListDTO;
import codeqr.code.dto.SessionListDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface StudentRepositoryCustom {
    List<StudentListDTO> fetchStudentsLight(Long cursorId, int limit, String q, Long specialtyId, Long levelId);
    List<EnrollmentListDTO> fetchEnrollmentsByStudent(Long studentId, int offset, int limit);
    List<SessionListDTO> fetchSessionsByEnrollment(Long studentYearProfileId, LocalDateTime start, LocalDateTime end, LocalDateTime lastStartTime, Long lastId, int limit);
}