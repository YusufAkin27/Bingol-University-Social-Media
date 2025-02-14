package bingol.campus.chat.converter;

import bingol.campus.chat.dto.MessageDTO;
import bingol.campus.chat.dto.PrivateChatDTO;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;
import org.springframework.stereotype.Component;

@Component
public class ChatConverterImpl implements ChatConverter {
    @Override
    public MessageDTO toMessageDTO(Message message) {
        long receiverId = 0;

        // Eğer mesajın ait olduğu sohbet PrivateChat ise, alıcıyı belirle
        if (message.getChat() instanceof PrivateChat) {
            PrivateChat privateChat = (PrivateChat) message.getChat();
            if (privateChat.getParticipant1().getStudent().getId().equals(message.getSender().getId())) {
                receiverId = privateChat.getParticipant2().getStudent().getId();
            } else {
                receiverId = privateChat.getParticipant1().getStudent().getId();
            }
        }

        return MessageDTO.builder()
                .chatId(message.getChat().getId())
                .messageId(message.getId())
                .content(message.getContent())
                .senderId(message.getSender().getId())
                .receiverId(receiverId)
                .sentAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .isPinned(message.getIsPinned())
                .isDeletedForSender(message.getIsDeletedForSender())
                .isDeletedForAll(message.getIsDeletedForAll())
                .build();
    }

    @Override
    public PrivateChatDTO toPrivateChatDTO(PrivateChat privateChat) {
        return PrivateChatDTO.builder()
                .username1(privateChat.getParticipant1().getStudent().getUsername())
                .chatId(privateChat.getId())
                .chatName(privateChat.getChatName())
                .lastEndMessage(toMessageDTO(privateChat.getMessages().getLast()))
                .username2(privateChat.getParticipant2().getStudent().getUsername())
                .chatPhoto(privateChat.getChatPhoto())
                .build();
    }
}
