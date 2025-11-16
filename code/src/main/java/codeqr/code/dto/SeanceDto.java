 package codeqr.code.dto;

import java.time.LocalDateTime;

public class SeanceDto {
    public Long id;
    public String date;          // YYYY-MM-DD
    public String heures;        // "HH:mm - HH:mm"
    public String courseName;
    public String levelName;
    public String specialtyName;
    public String roomName;
    public String campusName;
    public String teacherName;
    public int nbPresent;
    public int nbAbsent;

    public SeanceDto() {}
}
