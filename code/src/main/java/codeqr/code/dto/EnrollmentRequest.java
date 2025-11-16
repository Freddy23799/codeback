package codeqr.code.dto;
import lombok.Data;

@Data
public class EnrollmentRequest {
    private Long specialtyId;
    private Long levelId;
    private Long academicYearId;
}