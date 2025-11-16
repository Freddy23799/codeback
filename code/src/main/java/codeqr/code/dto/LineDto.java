package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineDto {
    private String jour;       // ex: "LUNDI"
    private String start;      // "08:00"
    private String end;        // "10:00"
    private Long courseId;
    private String courseName;
    private Long professorId;
    private String professorName; 
    private Long roomId;
    private String roomName;   
}
