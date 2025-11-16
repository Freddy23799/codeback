
package codeqr.code.dto;

import lombok.*;
import java.util.List;
@Data 
@NoArgsConstructor
 @AllArgsConstructor
public class SemainePayload {
    private String dateDebut; // "YYYY-MM-DD"
    private String dateFin;   // optional (front provides), we will validate/calc
}