package bingol.campus.chat.service;

import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.repository.ChatRepository;
import bingol.campus.chat.repository.MessageRepository;
import bingol.campus.chat.response.CreateChatRequest;
import bingol.campus.chat.response.MessageResponse;
import bingol.campus.chat.response.SendMessageRequest;
import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
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
public class ChatService {

    private final ChatRepository chatRepository;
    private final StudentRepository studentRepository;
    private final MessageRepository messageRepository;

    public ResponseMessage createChat(String username, CreateChatRequest request) throws StudentNotFoundException, BlockingBetweenStudent {
        Student student = studentRepository.getByUserNumber(username);
        Student student1 = studentRepository.getByUserNumber(request.getUsername());

        if (!isFollow(student, student1)) {
            throw new RuntimeException("You are not following this user");
        }
        isBlocked(student, student1);

        if (isChatExists(username, request.getUsername())) {
            throw new RuntimeException("Chat already exists");
        }

        Chat chat = Chat.builder()
                .student1(student)
                .student2(student1)
                .pinnedMessage(null)
                .lastMessage(null)
                .build();

        chatRepository.save(chat);
        return new ResponseMessage("Chat created", true);
    }

    public boolean isChatExists(String username, String username1) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        Student student1 = studentRepository.getByUserNumber(username1);
        List<Chat> chats = student.getChats();
        return chats.stream()
                .anyMatch(c -> c.getStudent1().getId().equals(student1.getId()) || c.getStudent2().getId().equals(student1.getId()));
    }

    public boolean isBlocked(Student student, Student student1) throws BlockingBetweenStudent {
        boolean isBlockedByStudent = student.getBlocked().stream()
                .anyMatch(f -> f.getBlocker().getUsername().equals(student1.getUsername()));
        boolean isBlockedByStudent1 = student1.getBlocked().stream()
                .anyMatch(f -> f.getBlocker().getUsername().equals(student.getUsername()));
        if (isBlockedByStudent || isBlockedByStudent1) {
            throw new BlockingBetweenStudent();
        }
        return false;
    }

    public boolean isFollow(Student student, Student student1) {
        return student.getFollowing().stream()
                .anyMatch(f -> f.getFollower().getUsername().equals(student1.getUsername()));
    }

    public DataResponseMessage getChatById(String username, Long chatId) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (!chat.getStudent1().equals(student) && !chat.getStudent2().equals(student)) {
            throw new RuntimeException("You are not a participant of this chat");
        }
        return new DataResponseMessage("Chat found", true, chat);
    }

    public DataResponseMessage getAllChats(String username) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        List<Chat> chats = student.getChats();
        return new DataResponseMessage("Chats found", true, chats);
    }

    public DataResponseMessage sendMessage(String username, SendMessageRequest request) throws StudentNotFoundException {
        Student sender = studentRepository.getByUserNumber(username);
        Chat chat = chatRepository.findById(request.getChatId()).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (!chat.getStudent1().equals(sender) && !chat.getStudent2().equals(sender)) {
            throw new RuntimeException("You are not a participant of this chat");
        }
        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .sentTime(LocalDateTime.now())
                .isRead(false)
                .build();
        messageRepository.save(message);
        return new DataResponseMessage("Message sent", true, message);
    }

    public DataResponseMessage updateMessage(String username, MessageResponse request) throws StudentNotFoundException {
        Student sender = studentRepository.getByUserNumber(username);
        Message message = messageRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.getSender().equals(sender)) {
            throw new RuntimeException("You are not the sender of this message");
        }
        message.setContent(request.getContent());
        message.setSentTime(LocalDateTime.now());
        messageRepository.save(message);
        return new DataResponseMessage("Message updated", true, message);
    }

 public ResponseMessage deleteMessage(String username, Long chatId, Long messageId) throws StudentNotFoundException {
            Student student = studentRepository.getByUserNumber(username);
            Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
            if (!chat.getStudent1().equals(student) && !chat.getStudent2().equals(student)) {
                throw new RuntimeException("You are not a participant of this chat");
            }
            Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
            if (!message.getSender().equals(student)) {
                throw new RuntimeException("You are not the sender of this message");
            }
            messageRepository.delete(message);
            return new ResponseMessage("Message deleted", true);
        }
}