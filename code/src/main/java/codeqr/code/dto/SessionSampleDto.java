// SessionSampleDto.java
package codeqr.code.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class SessionSampleDto {
    private Long sessionId;
    private String courseTitle;
    private LocalDateTime startTime;
    private String campusName;
    private String levelName;
    private String specialtyName;
    private String roomName;
}
