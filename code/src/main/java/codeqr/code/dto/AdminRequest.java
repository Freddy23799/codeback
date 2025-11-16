package codeqr.code.dto;
import lombok.Data;

@Data
public class AdminRequest {
    private String fullName;
    private String email;
        private String matricule;
    private Long  sexeId;
    private String username;
    private String password; // pour create / update (laisser vide = ne pas changer)
}