package bingol.campus.chat.business.abstracts;

import bingol.campus.chat.core.request.DeleteMessageRequest;
import bingol.campus.chat.core.request.SendMessageRequest;
import bingol.campus.chat.core.request.UpdateMessageRequest;
import bingol.campus.chat.core.response.GroupChatResponse;
import bingol.campus.chat.core.response.MessageResponse;
import bingol.campus.chat.core.response.PrivateChatResponse;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface PrivateChatService {
    PrivateChatResponse createPrivateChat(String username, String username1) throws StudentNotFoundException;

    List<PrivateChatResponse> getPrivateChats(String username) throws StudentNotFoundException;

    List<MessageResponse> getPrivateMessages(String username, Long chatId) throws StudentNotFoundException;

    MessageResponse sendPrivateMessage(String username, SendMessageRequest request) throws StudentNotFoundException;

    DataResponseMessage<MessageResponse> updateMessage(String username, UpdateMessageRequest request);

    ResponseMessage deleteMessage(String username, DeleteMessageRequest request);

    DataResponseMessage<List<String>> getOnlineFriends(String username);

    ResponseMessage deletePrivateChat(String username, Long chatId);

    DataResponseMessage<List<MessageResponse>> searchMessagesInPrivateChat(String username, Long chatId, String keyword);

    DataResponseMessage<List<MessageResponse>> getMessagesAfterInPrivateChat(String username, Long chatId, LocalDateTime timestamp);

    DataResponseMessage<List<MessageResponse>> getMessagesFromUserInPrivateChat(String username, Long chatId, Long senderId);

    DataResponseMessage<List<MessageResponse>> getLastMessagesInPrivateChat(String username, Long chatId, int limit);

    ResponseEntity<Boolean> isUserOnline(String username, String username1) throws StudentNotFoundException;

    List<ChatParticipant> getChatParticipants(Long chatId);

    boolean isUserInChat(String name, Long chatId);
}
