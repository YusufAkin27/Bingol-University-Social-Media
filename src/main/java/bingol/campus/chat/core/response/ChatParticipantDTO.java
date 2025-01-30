package bingol.campus.chat.core.response;

import lombok.Data;

@Data
public class ChatParticipantDTO {
    private Long id;
    private Long studentId;
    private String studentUsername;
}
