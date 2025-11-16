package codeqr.code.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableWeekResponse {
    private SemaineDto semaine;
    private List<EmploiDTOS> emplois;
}