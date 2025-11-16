package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDTO {
    private Long sessionId;
    private LocalDateTime startTime;
    private String courseTitle;      // renommé
    private String levelName;
    private String specialtyName;
    private String roomName;
    private String campusName;
    private String teacherFullName;
    private Long presentCount;
    private Long totalCount;
    private Boolean closed;
}
