



package codeqr.code.dto;

import lombok.*;

@Data
public class SessionLightDO {

    private Long id;
    private String date;
    private String hourRange;
    private String courseTitle;
    private String levelName;
    private String specialtyName;
    private String roomName;
    private String campusName;
    private String teacherName;
    private Long presentCount;
    private Long absentCount;

    // Constructeur correspondant exactement à la requête JPQL
    public SessionLightDO(Long id, String date, String hourRange, String courseTitle,
                          String levelName, String specialtyName, String roomName,
                          String campusName, String teacherName, Long presentCount, Long absentCount) {
        this.id = id;
        this.date = date;
        this.hourRange = hourRange;
        this.courseTitle = courseTitle;
        this.levelName = levelName;
        this.specialtyName = specialtyName;
        this.roomName = roomName;
        this.campusName = campusName;
        this.teacherName = teacherName;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
    }

    // Lombok @Data génère automatiquement les getters, setters, equals, hashCode et toString
}
