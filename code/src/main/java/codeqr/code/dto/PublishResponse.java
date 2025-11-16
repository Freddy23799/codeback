package codeqr.code.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data 
@NoArgsConstructor
 @AllArgsConstructor
public class PublishResponse {
    private Long semaineId;
    private List<EmploiInsertedInfo> emplois;
    private String message;
}