package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String type; // TEXT / IMAGE / AUDIO
    private String content;
    private String status; // SENT / DELIVERED / READ
    private LocalDateTime createdAt;
}
