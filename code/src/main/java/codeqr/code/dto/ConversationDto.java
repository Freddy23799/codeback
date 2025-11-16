package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    private Long id;
    private String name;
    private List<UserDto> participants;
    private List<MessageDto> messages;
    private int unreadCount; // Pour l'utilisateur courant
    private LocalDateTime updatedAt;
}
