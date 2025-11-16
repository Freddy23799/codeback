package codeqr.code.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishPayload {
    private SemainePayload semaine;
    private String createdBy; // string because front may send "123"
    private List<EmploiPayload> emplois;
}
