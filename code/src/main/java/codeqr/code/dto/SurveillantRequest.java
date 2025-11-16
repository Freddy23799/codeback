package codeqr.code.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class SurveillantRequest {
    private String username;
    private String password;
    private String fullName;
      private String matricule;
    
    private String email;
    private Long sexeId;   // ✅ ajouté

    // getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getSexeId() { return sexeId; }
    public void setSexeId(Long sexeId) { this.sexeId = sexeId; }

     public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

 
}
