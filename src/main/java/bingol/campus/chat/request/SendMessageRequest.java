package bingol.campus.chat.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private Long chatId;
    private String content;
}
