package codeqr.code.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherRequest {
    private String username;
    private String password;
    private String fullName;
    private String matricule;
    private String email;
    private Long sexeId;   // ✅ identifiant du sexe sélectionné

    // ✅ nouvelle propriété : liste d’IDs des cours sélectionnés
    private List<Long> courseIds; 
}
