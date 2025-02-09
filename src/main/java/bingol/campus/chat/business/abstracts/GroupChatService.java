package bingol.campus.chat.business.abstracts;

import bingol.campus.chat.core.request.AddParticipantRequest;
import bingol.campus.chat.core.request.CreateChatRequest;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.request.UpdateGroupRequest;
import bingol.campus.chat.core.response.GroupChatResponse;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupChatService  {
    DataResponseMessage<GroupChatResponse> createGroupChat(String username, CreateChatRequest request);

    DataResponseMessage<List<GroupChatResponse>> getGroupChats(String username);

    DataResponseMessage<List<MessageResponse>> getGroupMessages(String username, Long chatId);

    DataResponseMessage<MessageResponse> sendGroupMessage(String username, SendMessageRequest request);

    ResponseMessage addGroupParticipant(String username, AddParticipantRequest request);

    ResponseMessage removeGroupParticipant(String username, Long chatId, Long participantId);

    DataResponseMessage<List<Long>> getGroupParticipants(String username, Long chatId);

    ResponseMessage deleteGroupChat(String username, Long chatId);

    ResponseMessage leaveGroup(String username, Long chatId);

    ResponseMessage removeUserFromGroup(String username, Long chatId, Long participantId);

    DataResponseMessage<GroupChatResponse> updateGroupProfile(String username, Long chatId, UpdateGroupRequest request);

    ResponseMessage assignGroupAdmin(String username, Long chatId, Long userId);

    ResponseMessage removeGroupAdmin(String username, Long chatId, Long userId);

    DataResponseMessage<List<Long>> getGroupAdmins(String username, Long chatId);

    DataResponseMessage<List<MessageResponse>> searchMessagesInGroupChat(String username, Long chatId, String keyword);

    DataResponseMessage<List<MessageResponse>> getMessagesAfterInGroupChat(String username, Long chatId, LocalDateTime timestamp);

    DataResponseMessage<List<MessageResponse>> getMessagesFromUserInGroupChat(String username, Long chatId, Long senderId);

    DataResponseMessage<List<MessageResponse>> getLastMessagesInGroupChat(String username, Long chatId, int limit);
}
