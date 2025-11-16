package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor   // 🔹 génère le constructeur vide
@AllArgsConstructor  // 🔹 génère le constructeur complet
public class SessionPageResponse {
    private List<SessionDtoss> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
