package bingol.campus.chat.business.abstracts;

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

public interface PrivateChatService {
    DataResponseMessage<ChatResponse> createPrivateChat(String username, CreateChatRequest request);

    DataResponseMessage<List<ChatResponse>> getPrivateChats(String username);

    DataResponseMessage<List<MessageResponse>> getPrivateMessages(String username, Long chatId);

    DataResponseMessage<MessageResponse> sendPrivateMessage(String username, SendMessageRequest request);

    DataResponseMessage<MessageResponse> updateMessage(String username, UpdateMessageRequest request);

    ResponseMessage deleteMessage(String username, DeleteMessageRequest request);

    DataResponseMessage<List<String>> getOnlineFriends(String username);

    ResponseMessage deletePrivateChat(String username, Long chatId);

    DataResponseMessage<List<MessageResponse>> searchMessagesInPrivateChat(String username, Long chatId, String keyword);

    DataResponseMessage<List<MessageResponse>> getMessagesAfterInPrivateChat(String username, Long chatId, LocalDateTime timestamp);

    DataResponseMessage<List<MessageResponse>> getMessagesFromUserInPrivateChat(String username, Long chatId, Long senderId);

    DataResponseMessage<List<MessageResponse>> getLastMessagesInPrivateChat(String username, Long chatId, int limit);
}
