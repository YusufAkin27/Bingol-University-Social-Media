package bingol.campus.chat.core.converter;


import bingol.campus.chat.core.response.ChatParticipantDTO;
import bingol.campus.chat.core.response.MessageDTO;
import bingol.campus.chat.core.response.PrivateChatDTO;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatConverterImpl implements ChatConverter {
    @Override
    public PrivateChatDTO toPrivateChatDTO(PrivateChat chat) {
        PrivateChatDTO dto = new PrivateChatDTO();
        dto.setId(chat.getId());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setLastActiveAt(chat.getLastActiveAt());
        dto.setChatName(chat.getChatName());
        dto.setChatPhoto(chat.getChatPhoto());

        // Katılımcıları ID listesi olarak dönüyoruz
        List<Long> participantIds = chat.getParticipants()
                .stream()
                .map(p -> p.getStudent().getId())
                .collect(Collectors.toList());
        dto.setParticipantIds(participantIds);
        dto.setMessageDTOS(chat.getMessages().stream().map(this::toMessageDTO).toList());

        return dto;
    }

    @Override
    public ChatParticipantDTO toChatParticipantDTO(ChatParticipant chatParticipant) {
        ChatParticipantDTO dto = new ChatParticipantDTO();
        dto.setId(chatParticipant.getId()); // HATA: participant yerine chatParticipant olmalı!
        dto.setStudentId(chatParticipant.getStudent().getId());
        dto.setStudentUsername(chatParticipant.getStudent().getUsername());
        return dto;
    }

    @Override
    public MessageDTO toMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }


}
