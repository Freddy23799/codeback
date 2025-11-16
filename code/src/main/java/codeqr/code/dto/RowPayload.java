package codeqr.code.dto;
import java.util.*;
import lombok.*;
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class RowPayload {
    private String jour; // "LUNDI"
    private String start; // "08:00"
    private String end;   // "10:00"
    private Long courseId;
    private String courseName;
    private Long professorId;
    private String professorName;
    private Long roomId;
    private String roomName;
}