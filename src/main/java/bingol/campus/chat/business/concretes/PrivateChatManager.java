package bingol.campus.chat.business.concretes;

import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.converter.ChatConverter;
import bingol.campus.chat.dto.MessageDTO;
import bingol.campus.chat.dto.PrivateChatDTO;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.Notification;
import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.chat.exceptions.PrivateChatNotFoundException;
import bingol.campus.chat.repository.*;
import bingol.campus.chat.request.SendMessageRequest;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateChatManager implements PrivateChatService {
    private final ChatRepository chatRepository;
    private final StudentRepository studentRepository;
    private final MessageRepository messageRepository;
    private final PrivateChatRepository privateChatRepository;
    private final ChatConverter chatConverter;
    private final ChatParticipantRepository chatParticipantRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public DataResponseMessage sendPrivateMessage(String username, SendMessageRequest sendMessageRequest) throws StudentNotFoundException, PrivateChatNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        PrivateChat privateChat = privateChatRepository.findById(sendMessageRequest.getChatId()).orElseThrow(PrivateChatNotFoundException::new);
        Message message = new Message();
        message.setChat(privateChat);
        message.setContent(sendMessageRequest.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message.setSender(student);
        message.setIsDeletedForAll(false);
        message.setIsPinned(false);
        message.setMediaUrls(null);
        message.setSeenBy(null);
        message.setUpdatedAt(null);
        messageRepository.save(message);
        privateChat.getMessages().add(message);
        Notification notification = new Notification();
        notification.setChat(privateChat);
        notification.setSender(student);
        notification.setReceiver(privateChat.getParticipant1().getStudent().equals(student) ? privateChat.getParticipant2().getStudent() : privateChat.getParticipant1().getStudent());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setContent(message.getContent());
        notification.setIsRead(false);
        notificationRepository.save(notification);
        privateChatRepository.save(privateChat);

        return new DataResponseMessage("Message sent successfully", true, chatConverter.toMessageDTO(message));
    }

    @Override
    public ResponseMessage createChat(String username, String username1) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        Student student1 = studentRepository.getByUserNumber(username1);

        PrivateChat privateChat = new PrivateChat();
        privateChat.setChatName(student.getFirstName() + " " + student.getLastName() + " - " + student1.getFirstName() + " " + student1.getLastName());
        privateChat.setCreatedAt(LocalDateTime.now());
        privateChat.setUpdatedAt(LocalDateTime.now());

        ChatParticipant participant1 = new ChatParticipant();
        participant1.setChat(privateChat);
        participant1.setStudent(student);
        participant1.setIsAdmin(false);
        participant1.setNotificationsEnabled(false);
        participant1.setLastSeenAt(student.getLastSeenAt());

        ChatParticipant participant2 = new ChatParticipant();
        participant2.setChat(privateChat);
        participant2.setStudent(student1);
        participant2.setIsAdmin(false);
        participant2.setNotificationsEnabled(false);
        participant2.setLastSeenAt(student1.getLastSeenAt());

        privateChat.setParticipant1(participant1);
        privateChat.setParticipant2(participant2);

        privateChatRepository.save(privateChat);

        return new ResponseMessage("Chat created successfully", true);
    }


    @Override
    public DataResponseMessage getChats(String username) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);

        List<PrivateChatDTO> privateChatDTOS = student.getPrivateChats().stream().map(chatConverter::toPrivateChatDTO).toList();

        return new DataResponseMessage("Private chats fetched successfully", true, privateChatDTOS);
    }

    @Override
    public DataResponseMessage getMessages(String username, Long chatId) throws StudentNotFoundException, PrivateChatNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        PrivateChat privateChat = student.getPrivateChats().stream().filter(p -> p.getId().equals(chatId)).findFirst().orElseThrow(PrivateChatNotFoundException::new);
        List<MessageDTO>messageDTOS=privateChat.getMessages().stream().map(chatConverter::toMessageDTO).toList();
        return new DataResponseMessage("Messages fetched successfully", true, messageDTOS);
    }
}
