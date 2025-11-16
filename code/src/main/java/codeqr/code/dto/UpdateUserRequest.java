package codeqr.code.dto;
import lombok.*;

@Data
public class UpdateUserRequest {
    private Long id;      // id dans la table spécifique (Student, Prof, Admin, Surveillant)
    private String username;
    private String password;
    private String role;  // "ADMIN", "PROFESSEUR", "SURVEILLANT", "ETUDIANT"

    // getters et setters
}
