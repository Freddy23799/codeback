
package codeqr.code.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminDTO {
    private Long id;
    private String fullName;
    private String email;
   private String matricule;
    private String username;
    private String sexeName;

    public AdminDTO( String email,String matricule, String fullName, Long id, String sexeName, String username) {
     
        this.email = email;
    
        this.fullName = fullName;
        this.id = id;
        
        this.matricule = matricule;
        this.sexeName = sexeName;
        this.username = username;
    }

    public AdminDTO() {
        //TODO Auto-generated constructor stub
    }
}
