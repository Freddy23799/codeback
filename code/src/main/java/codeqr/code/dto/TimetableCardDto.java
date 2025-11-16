package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableCardDto {
    private Long id;                   // EmploiTemps.id (peut être null si brouillon côté client)
    private String clientId;           // identifiant client si besoin
    private Long specialtyId;
    private String specialtyLabel;    // si disponible (optionnel)
    private Long levelId;
    private String levelLabel;
    private String academicYear;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;
    private String createdByName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<LineDto> rows;
}
