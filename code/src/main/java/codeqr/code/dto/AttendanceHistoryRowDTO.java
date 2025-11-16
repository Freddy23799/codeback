package codeqr.code.dto;

import java.time.LocalDateTime;

public record AttendanceHistoryRowDTO(
        Long id,               // Attendance ID (row-key)
        String courseTitle,
        LocalDateTime sessionStart,
        String dayOfWeek,      // "lundi", "mardi", ...
        String campusName,
        String roomName,
        String teacherName,
        String status          // "PRESENT" | "ABSENT" | "PENDING"
) {}
