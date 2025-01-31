package bingol.campus.chat.business.concretes;

import bingol.campus.chat.core.converter.ChatConverter;
import bingol.campus.chat.core.response.MessageDTO;
import bingol.campus.chat.core.response.PrivateChatDTO;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.chat.repository.MessageRepository;
import bingol.campus.chat.repository.PrivateChatRepository;
import bingol.campus.student.entity.Student;
import bingol.campus.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageManager {

    private final MessageRepository messageRepository;
    private final PrivateChatRepository privateChatRepository;
    private final StudentRepository studentRepository;
    private final ChatConverter chatConverter;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageDTO sendMessage(Long receiverId, String content, UserDetails userDetails) {
        Student sender = studentRepository.findByUserNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Gönderici bulunamadı"));
        Student receiver = studentRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Alıcı bulunamadı"));

        // Önceden bir sohbet var mı kontrol et
        PrivateChat chat = privateChatRepository.findByParticipants(sender, receiver)
                .orElseGet(() -> createPrivateChat(sender, receiver));

        // Yeni mesaj oluştur
        Message message = Message.builder()
                .sender(sender)
                .chat(chat)
                .content(content)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        messageRepository.save(message);

        // WebSocket ile mesajı ilgili kullanıcılara ilet
        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), message);

        return chatConverter.toMessageDTO(message);
    }

    private PrivateChat createPrivateChat(Student sender, Student receiver) {
        PrivateChat newChat = new PrivateChat();
        newChat.setCreatedAt(LocalDateTime.now());

        // ChatParticipant ekleme
        ChatParticipant senderParticipant = new ChatParticipant();
        senderParticipant.setChat(newChat);
        senderParticipant.setStudent(sender);

        ChatParticipant receiverParticipant = new ChatParticipant();
        receiverParticipant.setChat(newChat);
        receiverParticipant.setStudent(receiver);

        newChat.setParticipants(List.of(senderParticipant, receiverParticipant));

        return privateChatRepository.save(newChat);
    }

    public PrivateChatDTO getPrivateChat(Long otherUserId, UserDetails userDetails) {
        Student currentUser = studentRepository.findByUserNumber(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Student otherUser = studentRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Diğer kullanıcı bulunamadı"));

        return privateChatRepository.findByParticipants(currentUser, otherUser).map(chatConverter::toPrivateChatDTO)
                .orElseThrow(() -> new RuntimeException("Bu kullanıcı ile sohbet bulunamadı"));
    }

}
