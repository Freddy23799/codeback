package codeqr.code.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TeacherListDTO {
    private Long id;
    private String fullName;
    private String matricule;
    private String email;

    // Liste de cours: chaque élément = {id, code, title}
    private List<Map<String, Object>> courses;

    // Ancien champ string si nécessaire pour compatibilité
    private String coursesTitles;

    private String sexeName;
    private Long sexeId;  // ajouté pour pré-remplir le select sexe
    private String username;
    private Integer profileCount;
}
