package codeqr.code.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session2DTO {
    private Long id;
    private Long courseId;
    private Long campusId;
    private String userUsername;
    private Long professorId;   // id du professeur sélectionné
    private Long surveillantId; 
    private Long roomId;
    private Long expectedLevelId;
    private Long expectedSpecialtyId;
    private Long teacherYearProfileId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String,Object> qrPayload;
     private String qrToken;
    }