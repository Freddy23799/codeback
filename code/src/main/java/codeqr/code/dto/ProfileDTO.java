package codeqr.code.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor  // constructeur vide
public class ProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private String matricule;
    private String matiere; // optionnel (null si pas prof)
    private String sexe;
    private String role;

    // Constructeur pour étudiant / admin (sans matiere)
    public ProfileDTO(Long id, String fullName, String email, String matricule, String sexe, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.matricule = matricule;
        this.sexe = sexe;
        this.role = role;
    }

    // Constructeur pour professeur (avec matiere)
    public ProfileDTO(Long id, String fullName, String email, String matricule, String matiere, String sexe, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.matricule = matricule;
        this.matiere = matiere;
        this.sexe = sexe;
        this.role = role;
    }
}
