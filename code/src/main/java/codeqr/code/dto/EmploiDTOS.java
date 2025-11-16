package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDTOS {
    private Long id;
    private Long specialiteId;
    private Long niveauId;
    private Long anneeAcademiqueId;
    private String status;
    private List<LigneDTOS> rows;
}
