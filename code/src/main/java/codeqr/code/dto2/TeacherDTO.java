package codeqr.code.dto2;

import java.util.List;

import codeqr.code.dto.CourseDO;

public class TeacherDTO {
    private Long id;
    private String fullName;
    private String email;
    private String matricule;
    private String sexeName; // libellé du sexe
    private String username;
    private Long sexeId;

    // ✅ On remplace "matiere" par une liste de cours
    private List<CourseDO> courses;

    public TeacherDTO() {}

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

    public List<CourseDO> getCourses() { return courses; }
    public void setCourses(List<CourseDO> courses) { this.courses = courses; }
}
