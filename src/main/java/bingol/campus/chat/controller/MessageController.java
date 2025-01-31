package bingol.campus.chat.controller;

import bingol.campus.chat.entity.Message;
import bingol.campus.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;

    @MessageMapping("/send")  // İstemciler "/app/send" endpointine mesaj gönderir
    @SendTo("/topic/messages") // Mesaj "/topic/messages" kanalına yayınlanır
    public Message sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);
        return message; // Mesajı tüm bağlı istemcilere geri döndürür
    }
}
