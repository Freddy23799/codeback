package codeqr.code.dto;
import java.util.*;
import lombok.*;
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class EmploiInsertedInfo {
    private Long emploiId;
    private Long specialiteId;
    private Long niveauId;
    private List<Long> ligneIds; // (optional) ids des lignes insérées (si besoin)
}