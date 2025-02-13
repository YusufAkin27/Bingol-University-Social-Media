package bingol.campus.chat.response;


import lombok.Data;



@Data
public class SendMessageRequest {


    private String content;
    private Long chatId;
}