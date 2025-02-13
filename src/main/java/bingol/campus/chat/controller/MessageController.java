package bingol.campus.chat.controller;

import bingol.campus.chat.entity.Message;
import bingol.campus.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        return messageService.sendMessage(message.getChat().getId(), message);
    }

    @MessageMapping("/updateMessage")
    @SendTo("/topic/messages")
    public Message updateMessage(Message message) {
        return messageService.updateMessage(message.getChat().getId(), message.getId(), message);
    }

    @MessageMapping("/deleteMessage")
    @SendTo("/topic/messages")
    public void deleteMessage(Long chatId, Long messageId) {
        messageService.deleteMessage(chatId, messageId);
    }
}