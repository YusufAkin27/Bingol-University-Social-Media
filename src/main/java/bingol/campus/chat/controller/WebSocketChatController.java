package bingol.campus.chat.controller;

import bingol.campus.chat.core.response.MessageResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{chatId}")
    public void sendMessageToChat(Long chatId, MessageResponse messageResponse) {
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageResponse);
    }
}
