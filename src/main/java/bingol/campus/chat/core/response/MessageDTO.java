package bingol.campus.chat.core.response;

import bingol.campus.chat.entity.Message;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;


}
