package bingol.campus.chat.controller;

import bingol.campus.chat.entity.Message;
import bingol.campus.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class PrivateMessageController {

    private final MessageRepository messageRepository;

    @MessageMapping("/private-message")
    @SendToUser("/queue/reply")
    public Message sendPrivateMessage(Message message, @Header("simpSessionId") String sessionId) {
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);
        return message; // Sadece belirli bir istemciye mesaj döndürür
    }
}
