package codeqr.code.dto;
import java.time.LocalDate;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcademicYearDto {
  
    private Long id;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}