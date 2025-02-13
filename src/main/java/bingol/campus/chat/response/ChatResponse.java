package bingol.campus.chat.response;

import lombok.Data;

@Data
public class ChatResponse {
    private Long id;
    private String chatName;
    private Long[] participantIds;
}