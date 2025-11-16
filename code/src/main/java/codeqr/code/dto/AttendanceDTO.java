package codeqr.code.dto;

import java.time.LocalDateTime;

import codeqr.code.model.Attendance.Status;
public class AttendanceDTO {
    private Long attendanceId;
    private Long sessionId;
    private Long profileId;
    private String status;
    private String source;
    private LocalDateTime scannedAt;

    public AttendanceDTO() {}

    public AttendanceDTO(Long attendanceId, Long sessionId, Long profileId,
                         String status, String source, LocalDateTime scannedAt) {
        this.attendanceId = attendanceId;
        this.sessionId = sessionId;
        this.profileId = profileId;
        this.status = status;
        this.source = source;
        this.scannedAt = scannedAt;
    }

    public AttendanceDTO(Long profileId, Long sessionId, Status status) {
        this.profileId = profileId;
        this.sessionId = sessionId;
        this.status = status.name();
    }

    // getters & setters
    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
}
