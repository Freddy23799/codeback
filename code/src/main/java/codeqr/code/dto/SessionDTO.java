package codeqr.code.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SessionDTO {
    private Long id;
        private Long courseId;
            private Long campusId;
                private Long roomId;
    private String date;        // ex "2025-09-15"
    private String heures;      // "08:00 - 10:00"
    private String courseName;
    private String levelName;
    private String specialtyName;
    private String roomName;
    private String campusName;
    private String teacherName;
        private String status;
        private String duration;
    private Integer nbPresent;
    private Integer nbAbsent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String,Object> qrPayload; // optionnel
}
