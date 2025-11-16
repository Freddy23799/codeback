package codeqr.code.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudentDTO {
    private Long id;
    private String fullName;
    private String email;
    private String matricule;
    private String username;
    private String sexeName;
    private List<EnrollmentDTO> enrollments;

    public StudentDTO( String email, List<EnrollmentDTO> enrollments, String fullName, Long id, String matricule, String sexeName, String username) {
     
        this.email = email;
        this.enrollments = enrollments;
        this.fullName = fullName;
        this.id = id;
        this.matricule = matricule;
        this.sexeName = sexeName;
        this.username = username;
    }

    public StudentDTO() {
        //TODO Auto-generated constructor stub
    }
}
