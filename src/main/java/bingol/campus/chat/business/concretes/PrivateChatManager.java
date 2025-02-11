package bingol.campus.chat.business.concretes;

import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.business.abstracts.WebSocketService;
import bingol.campus.chat.core.request.DeleteMessageRequest;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.request.UpdateMessageRequest;
import bingol.campus.chat.core.response.GroupChatResponse;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.chat.core.response.PrivateChatResponse;
import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.chat.repository.*;
import bingol.campus.config.ChatWebSocketHandler;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @Override
    @Transactional
    public DataResponseMessage<PrivateChatResponse> createPrivateChat(String username, Long userId) throws StudentNotFoundException {
        Student currentUser = studentRepository.getByUserNumber(username);
        Student targetUser = studentRepository.findById(userId)
                .orElseThrow(StudentNotFoundException::new);

        Optional<PrivateChat> existingChat = privateChatRepository.findByParticipants(currentUser, targetUser);
        if (existingChat.isPresent()) {
            return new DataResponseMessage<>("Mevcut sohbet bulundu.", true, mapToChatResponse(existingChat.get()));
        }

        // 2️⃣ - Yeni sohbet oluştur
        Chat newChat = new Chat();
        newChat.setCreatedAt(LocalDateTime.now());
        newChat.setLastActiveAt(LocalDateTime.now());
        chatRepository.save(newChat);

        PrivateChat privateChat = new PrivateChat();
        privateChat.setChatName(targetUser.getFirstName() + " " + targetUser.getLastName()); // Sohbet adını karşıdaki kişinin adı yap
        privateChat.setChatPhoto(targetUser.getProfilePhoto()); // Profil fotoğrafı olarak karşıdakinin profilini kullan
        privateChatRepository.save(privateChat);

        // 3️⃣ - Katılımcıları ekle
        ChatParticipant participant1 = new ChatParticipant();
        participant1.setJoinedAt(LocalDateTime.now());
        participant1.setChat(privateChat);
        participant1.setStudent(currentUser);
        ChatParticipant participant2 = new ChatParticipant();
        participant2.setStudent(targetUser);
        participant2.setChat(privateChat);
        participant1.setJoinedAt(LocalDateTime.now());
        chatParticipantRepository.saveAll(List.of(participant1, participant2));


        return new DataResponseMessage<>("Yeni özel sohbet oluşturuldu.", true, mapToChatResponse(privateChat));
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
    public DataResponseMessage<List<PrivateChatResponse>> getPrivateChats(String username) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);

        List<PrivateChatResponse> privateChats = student.getChatParticipants().stream()
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

        return new DataResponseMessage<>("Özel sohbetler başarıyla getirildi", true, privateChats);
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getPrivateMessages(String username, Long chatId) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);

        boolean isParticipant = student.getChatParticipants().stream()
                .anyMatch(participant -> participant.getChat().getId().equals(chatId));

        if (!isParticipant) {
            return new DataResponseMessage<>("Bu sohbete erişiminiz yok!", false, null);
        }

        PrivateChat privateChat = (PrivateChat) chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Sohbet bulunamadı"));

        List<MessageResponse> messages = privateChat.getMessages().stream()
                .map(message -> MessageResponse.builder()
                        .messageId(message.getId())
                        .username(message.getSender().getUserNumber())
                        .content(message.getContent())
                        .timestamp(message.getTimestamp())
                        .isRead(message.isRead())
                        .build())
                .toList();

        return new DataResponseMessage<>("Özel sohbet mesajları başarıyla getirildi", true, messages);
    }


    @Override
    public DataResponseMessage<MessageResponse> sendPrivateMessage(String username, SendMessageRequest request) {
        Student sender = studentRepository.findById(request.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Gönderici kullanıcı bulunamadı"));

        PrivateChat privateChat;

        if (request.getChatId() == null || request.getChatId() == 0) {
            privateChat = new PrivateChat();
            privateChat.setCreatedAt(LocalDateTime.now());
            privateChat.setLastActiveAt(LocalDateTime.now());
            privateChat.setChatName("Özel Sohbet"); // Varsayılan isim

            privateChat = chatRepository.save(privateChat);

            ChatParticipant senderParticipant = new ChatParticipant(null, privateChat, sender, LocalDateTime.now());
            chatParticipantRepository.save(senderParticipant);

            // Sohbetin diğer katılımcısını belirleme (Burada mantığa göre değişebilir)
            Student receiver = studentRepository.findByUserNumber(username)
                    .orElseThrow(() -> new IllegalArgumentException("Alıcı bulunamadı"));

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

        MessageResponse messageResponse = new MessageResponse(
                message.getId(),
                sender.getUserNumber(),
                message.getContent(),
                message.getTimestamp(),
                message.isRead()
        );

        List<ChatParticipant> participants = privateChat.getParticipants();
        for (ChatParticipant participant : participants) {
            if (!participant.getStudent().equals(sender)) {
                webSocketService.sendMessageToUser(participant.getStudent().getUserNumber(), messageResponse.getContent());
            }
        }

        return new DataResponseMessage<>("Mesaj başarıyla gönderildi", true, messageResponse);
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
