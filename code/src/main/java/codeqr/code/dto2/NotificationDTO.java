package codeqr.code.dto2;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long destinataireId;
    private String destinataireUsername;
    private String destinataireRole;

    public NotificationDTO(Long id, String title, String message, boolean read,
                           LocalDateTime createdAt, Long destinataireId,
                           String destinataireUsername, String destinataireRole) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.destinataireId = destinataireId;
        this.destinataireUsername = destinataireUsername;
        this.destinataireRole = destinataireRole;
    }

    // Getters / Setters ou Lombok @Data
}
