package bingol.campus.chat.core.request;

import lombok.Data;

@Data
public class UpdateMessageRequest {
    private Long messageId;
    private String newContent;
}
