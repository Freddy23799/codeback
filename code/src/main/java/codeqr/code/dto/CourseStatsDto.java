// CourseStatsDto.java
package codeqr.code.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class CourseStatsDto {
    private Long teacherId;
    private String teacherName;
    private Integer totalSessions;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private List<CampusCountDto> sessionsByCampus;
    private List<SessionSampleDto> sessionsSample;
}
