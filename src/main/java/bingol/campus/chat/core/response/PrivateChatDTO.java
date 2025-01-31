package bingol.campus.chat.core.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrivateChatDTO {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private String chatName;
    private String chatPhoto;
    private List<Long> participantIds; // Katılımcıların sadece ID'leri
    private List<MessageDTO>messageDTOS;
}
