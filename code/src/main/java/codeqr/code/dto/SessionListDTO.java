package codeqr.code.dto;

import java.time.LocalDateTime;

public class SessionListDTO {
    public Long sessionId;
    public String courseTitle;
    public String campusName;
    public String roomName;
    public String teacherName;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Integer attendanceCount;
    public String attendanceStatus;
}
