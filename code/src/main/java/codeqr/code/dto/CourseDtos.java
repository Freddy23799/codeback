package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseDtos {
    private Long id;
    private String title; // ton front attend "title" pour le option-label
    private String code;  // optionnel mais utile
}
