package codeqr.code.dto;

import lombok.*;
import java.util.List;
@Data 
@NoArgsConstructor
 @AllArgsConstructor
public class EmploiPayload {
    private Long specialiteId;
    private Long niveauId;
    private Long anneeAcademiqueId;
    private String title;
      private String status;
        private String notes;
    private List<RowPayload> rows;
}