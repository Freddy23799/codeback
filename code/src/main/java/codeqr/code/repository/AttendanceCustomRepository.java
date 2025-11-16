package codeqr.code.repository;

import java.util.Map;

public interface AttendanceCustomRepository {
    Map<String, Long> countAttendanceBySession(Long sessionId);
}
