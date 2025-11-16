package codeqr.code.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import codeqr.code.model.Attendance;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDTOS {
    private Long attendanceId;
    private Long studentYearProfileId;
    private String studentFullName;
    private Attendance.Status status;
    private LocalDateTime scannedAt;
}
