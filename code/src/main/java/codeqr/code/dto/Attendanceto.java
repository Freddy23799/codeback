package codeqr.code.dto;

import java.time.LocalDateTime;
import codeqr.code.model.Attendance.Status;

public record Attendanceto(
    Long attendanceId,
    String studentFullName,
    Status status,
    LocalDateTime scannedAt
) {}
