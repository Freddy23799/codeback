// TeacherListItemDto.java
package codeqr.code.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class TeacherListItemDto {
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String matricule;
    private Integer sessionsCount;
}
