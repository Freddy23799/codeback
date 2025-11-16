package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentWithEnrollmentsDTO {
    private Long id;
    private String fullName;
    private String matricule;
    private String email;
    private String username;
    private String sexeName;
    private List<EnrollmentDTO> enrollments;
}
