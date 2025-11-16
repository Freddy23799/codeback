package codeqr.code.dto;

public class TeacherDTO {
    private Long id;
    private String fullName;
    private String email;
    private String matricule;
    private String sexeName; // libellé du sexe
    private String username;
    private Long sexeId;
    private String matiere;

    public TeacherDTO() {
    }

    // public TeacherDTO(Long id, String fullName, String email, String matricule, String sexeName, String username, Long sexeId, String matiere) {
    //     this.id = id;
    //     this.fullName = fullName;
    //     this.email = email;
    //     this.matricule = matricule;
    //     this.sexeName = sexeName;
    //     this.username = username;
    //     this.sexeId = sexeId;
    //     this.matiere = matiere;
    // }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getSexeName() { return sexeName; }
    public void setSexeName(String sexeName) { this.sexeName = sexeName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getSexeId() { return sexeId; }
    public void setSexeId(Long sexeId) { this.sexeId = sexeId; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
}
