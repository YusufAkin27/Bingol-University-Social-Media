package bingol.campus.chat.core.converter;


import bingol.campus.chat.core.response.ChatParticipantDTO;
import bingol.campus.chat.core.response.MessageDTO;
import bingol.campus.chat.core.response.PrivateChatDTO;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.chat.entity.PrivateChat;


public interface ChatConverter {
    PrivateChatDTO toPrivateChatDTO(PrivateChat privateChat);
    ChatParticipantDTO toChatParticipantDTO(ChatParticipant chatParticipant);
    MessageDTO toMessageDTO(Message message);

}
