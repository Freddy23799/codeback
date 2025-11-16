// file: codeqr/code/dto/SurveillantListDTO.java
package codeqr.code.dto;

import lombok.Data;

@Data
public class ResponsableListDTO {
    private Long id;
    private String matricule;
    private String fullName;
    private String email;
    private String username; // pris de User
    private String sexeName;
}
