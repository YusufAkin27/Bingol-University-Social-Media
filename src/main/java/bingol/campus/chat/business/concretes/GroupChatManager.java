package bingol.campus.chat.business.concretes;

import bingol.campus.chat.business.abstracts.GroupChatService;
import bingol.campus.chat.core.request.AddParticipantRequest;
import bingol.campus.chat.core.request.CreateChatRequest;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.request.UpdateGroupRequest;
import bingol.campus.chat.core.response.ChatResponse;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;

import java.time.LocalDateTime;
import java.util.List;

public class GroupChatManager implements GroupChatService {
    @Override
    public DataResponseMessage<ChatResponse> createGroupChat(String username, CreateChatRequest request) {
        return null;
    }

    @Override
    public DataResponseMessage<List<ChatResponse>> getGroupChats(String username) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getGroupMessages(String username, Long chatId) {
        return null;
    }

    @Override
    public DataResponseMessage<MessageResponse> sendGroupMessage(String username, SendMessageRequest request) {
        return null;
    }

    @Override
    public ResponseMessage addGroupParticipant(String username, AddParticipantRequest request) {
        return null;
    }

    @Override
    public ResponseMessage removeGroupParticipant(String username, Long chatId, Long participantId) {
        return null;
    }

    @Override
    public DataResponseMessage<List<Long>> getGroupParticipants(String username, Long chatId) {
        return null;
    }

    @Override
    public ResponseMessage deleteGroupChat(String username, Long chatId) {
        return null;
    }

    @Override
    public ResponseMessage leaveGroup(String username, Long chatId) {
        return null;
    }

    @Override
    public ResponseMessage removeUserFromGroup(String username, Long chatId, Long participantId) {
        return null;
    }

    @Override
    public DataResponseMessage<ChatResponse> updateGroupProfile(String username, Long chatId, UpdateGroupRequest request) {
        return null;
    }

    @Override
    public ResponseMessage assignGroupAdmin(String username, Long chatId, Long userId) {
        return null;
    }

    @Override
    public ResponseMessage removeGroupAdmin(String username, Long chatId, Long userId) {
        return null;
    }

    @Override
    public DataResponseMessage<List<Long>> getGroupAdmins(String username, Long chatId) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> searchMessagesInGroupChat(String username, Long chatId, String keyword) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getMessagesAfterInGroupChat(String username, Long chatId, LocalDateTime timestamp) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getMessagesFromUserInGroupChat(String username, Long chatId, Long senderId) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getLastMessagesInGroupChat(String username, Long chatId, int limit) {
        return null;
    }
}
