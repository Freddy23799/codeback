package codeqr.code.dto;

import java.time.LocalDateTime;

public class SessionLightDT {
    public Long sessionId;
    public String courseTitle;
    public String campusName;
    public String roomName;
    public String teacherName;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String specialtyName;
    public String levelName;
    public Integer attendanceCount; // nombre de présences enregistrées
}
