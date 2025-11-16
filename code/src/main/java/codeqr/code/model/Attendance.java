package codeqr.code.model;

import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
// import java.util.List;

@Data

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"studentYearProfile_id", "session_id"})})
public class Attendance {
public Attendance(Session session2, StudentYearProfile student) {
//TODO Auto-generated constructor stub
}

public Attendance() {  
    //TODO Auto-generated constructor stub  
}  

public Attendance(StudentYearProfile profile, Session session2) {  
    //TODO Auto-generated constructor stub  
}  

@Id @GeneratedValue(strategy = GenerationType.IDENTITY)  
private Long id;  

@ManyToOne(optional = false)  
@JoinColumn(name = "studentYearProfile_id")  
private StudentYearProfile studentYearProfile;  

@ManyToOne(optional = false, fetch=FetchType.LAZY)  
@JoinColumn(name = "session_id")  
private Session session;  

@Column(nullable = false)  
@Enumerated(EnumType.STRING)  
private Status status;  

private LocalDateTime scannedAt;

private Instant timestamp;
@Enumerated(EnumType.STRING)
private Source source;
// @Transient // (Jakarta/JPA)
// @JsonIgnore // (Jackson)
private LocalDateTime ScanneedAt;  

public void setStudentYearProfileId(Long sid) {  
    throw new UnsupportedOperationException("Not supported yet.");  
}  

public void setSessionId(Long sessionId) {  
    throw new UnsupportedOperationException("Not supported yet.");  
}  

public enum Status {  
    PRESENT, ABSENT, PENDING  
}  

public enum Source {  
    qr, manual, offline  
}  

// Getters / Setters

}