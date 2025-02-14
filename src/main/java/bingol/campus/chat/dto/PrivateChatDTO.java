package bingol.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrivateChatDTO {
    private long chatId;

    private String chatName;
    private String chatPhoto;

    private String username1;

    private String username2;

    private MessageDTO lastEndMessage;
}
