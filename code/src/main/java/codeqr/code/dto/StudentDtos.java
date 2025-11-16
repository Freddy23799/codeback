package codeqr.code.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDtos {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    // ajoute d'autres champs si besoin (matricule, level, etc.)
}
