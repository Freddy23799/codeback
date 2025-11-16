package codeqr.code.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AcademicYearCreateDto {
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}
