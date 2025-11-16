package codeqr.code.dto;



import java.util.ArrayList;
import java.util.List;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentListDTO {
    public Long id;
    public String fullName;
    public String matricule;
    public String email;
    public String avatarUrl;
    public String sexeName;
    public Integer enrollmentCount;
    public List<PreviewEnrollmentDTO> previewEnrollments; // top N (ex: 1-2)










    public StudentListDTO(Long id, String fullName, String matricule, String email, Long sexeId, String sexeName) {
        this.id = id;
        this.fullName = fullName;
        this.matricule = matricule;
        this.email = email;
        this.avatarUrl = null;
        this.sexeName = sexeName;
        this.enrollmentCount = 0;
        this.previewEnrollments = new ArrayList<>();
    }
}
