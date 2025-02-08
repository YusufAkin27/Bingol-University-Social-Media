package bingol.campus.chat.core.request;

import lombok.Data;

@Data
public class AddParticipantRequest {
    private Long chatId;
    private Long participantId;
}
