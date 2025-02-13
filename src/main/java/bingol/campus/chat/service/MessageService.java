package bingol.campus.chat.service;

        import bingol.campus.chat.entity.Chat;
        import bingol.campus.chat.entity.Message;
        import bingol.campus.chat.repository.ChatRepository;
        import bingol.campus.chat.repository.MessageRepository;
        import bingol.campus.student.entity.Student;
        import bingol.campus.student.repository.StudentRepository;
        import lombok.RequiredArgsConstructor;
        import org.springframework.stereotype.Service;

        import java.time.LocalDateTime;

        @Service
        @RequiredArgsConstructor
        public class MessageService {

            private final MessageRepository messageRepository;
            private final ChatRepository chatRepository;
            private final StudentRepository studentRepository;

            public Message sendMessage(Long chatId, Message message) {
                Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
                Student sender = studentRepository.findById(message.getSender().getId()).orElseThrow(() -> new RuntimeException("Sender not found"));
                message.setChat(chat);
                message.setSender(sender);
                message.setSentTime(LocalDateTime.now());
                return messageRepository.save(message);
            }

            public Message updateMessage(Long chatId, Long messageId, Message message) {
                Message existingMessage = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
                existingMessage.setContent(message.getContent());
                existingMessage.setSentTime(LocalDateTime.now());
                return messageRepository.save(existingMessage);
            }

            public void deleteMessage(Long chatId, Long messageId) {
                Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
                messageRepository.delete(message);
            }
        }