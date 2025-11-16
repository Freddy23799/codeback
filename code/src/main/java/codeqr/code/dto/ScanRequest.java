package codeqr.code.dto;

import java.time.*;
import lombok.*;
import codeqr.code.model.Attendance;
// import java.time.*;
public class ScanRequest {
    private Long sessionId; // facultatif si token fourni
    private String qrToken; // facultatif si sessionId fourni
    private Long studentYearProfileId;
    private Attendance.Source source = Attendance.Source.qr; // qr/manual/offline
    private LocalDateTime scannedAt; // horodatage local (utile pour offline)
 private boolean present; 
 private  Instant timestamp;// remplace isPresent()
    
    private Long studentId;
    // getters et setters
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }



     public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    // getters / setters
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }


     public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    
    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
    public Long getStudentYearProfileId() { return studentYearProfileId; }
    public void setStudentYearProfileId(Long studentYearProfileId) { this.studentYearProfileId = studentYearProfileId; }
    public Attendance.Source getSource() { return source; }
    public void setSource(Attendance.Source source) { this.source = source; }
    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
}
