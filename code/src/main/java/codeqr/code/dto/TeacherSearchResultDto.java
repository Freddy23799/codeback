// TeacherSearchResultDto.java
package codeqr.code.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class TeacherSearchResultDto {
    private List<TeacherListItemDto> items;
    private Long nextCursor; // null si fin
    private Integer totalCount; // optional estimation
}
