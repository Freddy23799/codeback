package codeqr.code.dto;
import lombok.Data;

@Data
public class StudentRequest {
    private String fullName;
    private String email;
    private Long  sexeId;
     private String matricule;
    private String username;
    private String password; // pour create / update (laisser vide = ne pas changer)
}