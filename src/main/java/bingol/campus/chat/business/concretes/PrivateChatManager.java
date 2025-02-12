package bingol.campus.chat.business.concretes;

import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.business.abstracts.WebSocketService;
import bingol.campus.chat.core.request.DeleteMessageRequest;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.request.UpdateMessageRequest;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.chat.core.response.PrivateChatResponse;
import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.chat.repository.*;
import bingol.campus.chat.config.ChatWebSocketHandler;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateChatManager implements PrivateChatService {
    private final StudentRepository studentRepository;
    private final PrivateChatRepository privateChatRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatMediaRepository chatMediaRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public PrivateChatResponse createPrivateChat(String username, String jsonPayload) throws StudentNotFoundException {
        try {
            // JSON içinden "participantUsername" değerini al
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);
            String participantUsername = jsonNode.get("participantUsername").asText();

            // Kullanıcıları bul
            Student currentUser = studentRepository.getByUserNumber(username);
            Student targetUser = studentRepository.getByUserNumber(participantUsername);

            // Eğer bu iki kullanıcı arasında zaten bir özel sohbet varsa, direkt onu döndür
            Optional<PrivateChat> existingChat = privateChatRepository.findByParticipants(currentUser, targetUser);
            if (existingChat.isPresent()) {
                return mapToChatResponse(existingChat.get());
            }

            // Yeni sohbet oluştur
            PrivateChat privateChat = new PrivateChat();
            privateChat.setChatName(targetUser.getFirstName() + " " + targetUser.getLastName());
            privateChat.setChatPhoto(targetUser.getProfilePhoto());
            privateChat.setCreatedAt(LocalDateTime.now());
            privateChat.setLastActiveAt(LocalDateTime.now());
            privateChatRepository.save(privateChat);

            return mapToChatResponse(privateChat);
        } catch (Exception e) {
            throw new RuntimeException("Geçersiz JSON formatı: " + e.getMessage());
        }
    }

    private PrivateChatResponse mapToChatResponse(PrivateChat privateChat) {
        List<String> participantUsernames = privateChat.getParticipants().stream()
                .map(cp -> cp.getStudent().getUsername())
                .toList();

        PrivateChatResponse response = new PrivateChatResponse();
        response.setChatId(privateChat.getId());
        response.setCreatedAt(privateChat.getCreatedAt());
        response.setLastActiveAt(privateChat.getLastActiveAt());
        response.setParticipantUsername(String.join(", ", participantUsernames)); // String olarak döndür

        response.setChatName(privateChat.getChatName()); // Önceden belirlediğimiz sohbet adını kullan
        response.setChatPhoto(privateChat.getChatPhoto()); // Profil fotoğrafını ayarla

        return response;
    }


    @Override
    public List<PrivateChatResponse> getPrivateChats(String username) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);

        return student.getChatParticipants().stream()
                .map(ChatParticipant::getChat)
                .filter(chat -> chat instanceof PrivateChat)
                .map(chat -> (PrivateChat) chat)
                .map(privateChat -> PrivateChatResponse.builder()
                        .chatId(privateChat.getId())
                        .chatName(privateChat.getChatName())
                        .createdAt(privateChat.getCreatedAt())
                        .lastActiveAt(privateChat.getLastActiveAt())
                        .chatPhoto(privateChat.getChatPhoto())
                        .participantUsername(username)
                        .build())
                .toList();
    }

    @Override
    public List<MessageResponse> getPrivateMessages(String username, Long chatId) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);

        boolean isParticipant = student.getChatParticipants().stream()
                .anyMatch(participant -> participant.getChat().getId().equals(chatId));

        if (!isParticipant) {
            throw new IllegalArgumentException("Bu sohbete erişiminiz yok!");
        }

        PrivateChat privateChat = (PrivateChat) chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Sohbet bulunamadı"));

        return privateChat.getMessages().stream()
                .map(message -> MessageResponse.builder()
                        .messageId(message.getId())
                        .username(message.getSender().getUserNumber())
                        .content(message.getContent())
                        .timestamp(message.getTimestamp())
                        .isRead(message.isRead())
                        .build())
                .toList();
    }



    @Override
    public MessageResponse sendPrivateMessage(String username, SendMessageRequest request) throws StudentNotFoundException {
        Student sender = studentRepository.getByUserNumber(username);
        PrivateChat privateChat;

        // Eğer chatId gönderilmemişse yeni sohbet oluştur
        if (request.getChatId() == null || request.getChatId() == 0) {
            privateChat = new PrivateChat();
            privateChat.setCreatedAt(LocalDateTime.now());
            privateChat.setLastActiveAt(LocalDateTime.now());
            privateChat.setChatName("Özel Sohbet");

            privateChat = chatRepository.save(privateChat);

            ChatParticipant senderParticipant = new ChatParticipant(null, privateChat, sender, LocalDateTime.now());
            chatParticipantRepository.save(senderParticipant);

            // Alıcıyı belirle
            Student receiver = studentRepository.getByUserNumber(request.getUsername());
            ChatParticipant receiverParticipant = new ChatParticipant(null, privateChat, receiver, LocalDateTime.now());
            chatParticipantRepository.save(receiverParticipant);
        } else {
            privateChat = (PrivateChat) chatRepository.findById(request.getChatId())
                    .orElseThrow(() -> new IllegalArgumentException("Sohbet bulunamadı"));
        }

        Message message = new Message();
        message.setSender(sender);
        message.setChat(privateChat);
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        message = messageRepository.save(message);

        privateChat.setLastActiveAt(LocalDateTime.now());
        privateChat.setLastMessage(message);
        chatRepository.save(privateChat);

        return new MessageResponse(
                message.getId(),
                sender.getUserNumber(),
                message.getContent(),
                message.getTimestamp(),
                message.isRead()
        );
    }

    public List<ChatParticipant> getChatParticipants(Long chatId) {
        return chatParticipantRepository.findByChatId(chatId);
    }

    @Override
    public boolean isUserInChat(String name, Long chatId) {
        Optional<Chat> chatOptional = chatRepository.findById(chatId);

        if (chatOptional.isEmpty()) {
            return false;
        }

        Chat chat = chatOptional.get();

        return chat.getParticipants().stream()
                .anyMatch(p -> p.getStudent().getUsername().equals(name)); // Kullanıcı varsa true döndür
    }



    @Override
    public DataResponseMessage<MessageResponse> updateMessage(String username, UpdateMessageRequest request) {
        return null;
    }

    @Override
    public ResponseMessage deleteMessage(String username, DeleteMessageRequest request) {
        return null;
    }

    @Override
    public DataResponseMessage<List<String>> getOnlineFriends(String username) {
        return null;
    }

    @Override
    public ResponseMessage deletePrivateChat(String username, Long chatId) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> searchMessagesInPrivateChat(String username, Long chatId, String keyword) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getMessagesAfterInPrivateChat(String username, Long chatId, LocalDateTime timestamp) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getMessagesFromUserInPrivateChat(String username, Long chatId, Long senderId) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getLastMessagesInPrivateChat(String username, Long chatId, int limit) {
        return null;
    }

    @Override
    public ResponseEntity<Boolean> isUserOnline(String username, String username1) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        Student student1 = studentRepository.getByUserNumber(username1);
        if (student1==null){
            return ResponseEntity.ok(false);
        }
        boolean isOnline = ChatWebSocketHandler.getOnlineUsers().contains(student1.getUsername());
        return ResponseEntity.ok(isOnline);
    }


}
