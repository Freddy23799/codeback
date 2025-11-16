package codeqr.code.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor   // 👈 indispensable pour Jackson
@AllArgsConstructor
public class SessionDtoss {
    private Long id;
    private String date;        // ex: formaté en yyyy-MM-dd
    private String heures;      // ex: startTime - endTime
    private String courseName;
    private String levelName;
    private String specialtyName;
    private String roomName;
    private String campusName;
    private Long nbPresent;
    private Long nbAbsent;
}
