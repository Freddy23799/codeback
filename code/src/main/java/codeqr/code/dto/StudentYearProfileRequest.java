package codeqr.code.dto;

import lombok.Data;

@Data
public class StudentYearProfileRequest {

    private Long studentId;        // ID de l'étudiant
    private Long academicYearId;   // ID de l'année académique
    private boolean active;        // Indique si le profil est actif ou non

    // Si tu veux plus tard ajouter possibilité de changer level/specialty :
    private Long levelId;          // facultatif : ID du niveau
    private Long specialtyId;      // facultatif : ID de la spécialité
}
