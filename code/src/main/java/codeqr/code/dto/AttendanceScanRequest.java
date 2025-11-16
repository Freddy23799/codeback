package codeqr.code.dto;

import java.time.LocalDateTime;

public class AttendanceScanRequest {
    private String qrToken;
    private Long sessionId;
    private Long studentId;
    private LocalDateTime scannedAt;
    private String source;

    // Getters & Setters
    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Long getSessionId() {
        return sessionId;
    }


    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
