package codeqr.code.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemaineDto {
    private Long id;
    private String dateDebut; // "YYYY-MM-DD"
    private String dateFin;   // "YYYY-MM-DD"
}