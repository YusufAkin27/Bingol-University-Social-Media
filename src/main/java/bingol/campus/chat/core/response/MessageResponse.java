package bingol.campus.chat.core.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long messageId;
    private String username;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
}
