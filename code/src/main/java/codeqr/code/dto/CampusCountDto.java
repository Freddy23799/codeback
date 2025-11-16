// CampusCountDto.java
package codeqr.code.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CampusCountDto {
    private String campusName;
    private Long count;
}
