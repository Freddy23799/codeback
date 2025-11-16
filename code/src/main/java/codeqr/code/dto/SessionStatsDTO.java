// DTO minimal pour la vue
package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionStatsDTO {
    private Long sessionId;
    private String courseName;
    private String teacherName;
    private int nbPresent;
    private int nbAbsent;
}
