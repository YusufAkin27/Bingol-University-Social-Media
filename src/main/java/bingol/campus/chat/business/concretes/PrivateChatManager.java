package bingol.campus.chat.business.concretes;

import bingol.campus.chat.business.abstracts.PrivateChatService;
import bingol.campus.chat.core.request.CreateChatRequest;
import bingol.campus.chat.core.request.DeleteMessageRequest;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.request.UpdateMessageRequest;
import bingol.campus.chat.core.response.ChatResponse;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;

import java.time.LocalDateTime;
import java.util.List;

public class PrivateChatManager implements PrivateChatService {
    @Override
    public DataResponseMessage<ChatResponse> createPrivateChat(String username, CreateChatRequest request) {
        return null;
    }

    @Override
    public DataResponseMessage<List<ChatResponse>> getPrivateChats(String username) {
        return null;
    }

    @Override
    public DataResponseMessage<List<MessageResponse>> getPrivateMessages(String username, Long chatId) {
        return null;
    }

    @Override
    public DataResponseMessage<MessageResponse> sendPrivateMessage(String username, SendMessageRequest request) {
        return null;
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
}
