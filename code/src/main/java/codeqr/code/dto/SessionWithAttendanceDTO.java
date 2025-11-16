package codeqr.code.dto;

import java.time.LocalDateTime;

public class SessionWithAttendanceDTO {
    private Long id;
    private Long courseId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomName;
    private String attendanceStatus; // present / absent / pending
    private String attendanceSource; // qr / manual / offline
    private LocalDateTime scannedAt;
    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public String getAttendanceSource() { return attendanceSource; }
    public void setAttendanceSource(String attendanceSource) { this.attendanceSource = attendanceSource; }
    public java.time.LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(java.time.LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
}
