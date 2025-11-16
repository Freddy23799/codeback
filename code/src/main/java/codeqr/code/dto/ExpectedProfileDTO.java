package codeqr.code.dto;
import lombok.*;
import codeqr.code.model.Attendance;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpectedProfileDTO {
    private Long profileId;
    private Long studentId;
    private String fullName;
    private String matricule;
    private Long levelId;
    private Long specialtyId;
    private Long academicYearId;
    private Attendance.Status status;
    private Attendance.Source source;
}
