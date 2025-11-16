
package codeqr.code.dto;

import java.time.LocalDateTime;
import lombok.*;


@Data
@AllArgsConstructor
public class NotificationDT {
    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

   }
