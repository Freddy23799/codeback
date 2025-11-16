package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDto {
    private Long id;
    private String matricule;
    private String fullName;
    private String email;
}
