package bingol.campus.chat.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String content;
    private LocalDateTime sentTime;
}