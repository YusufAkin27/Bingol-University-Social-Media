package bingol.campus.chat.business.abstracts;

import bingol.campus.chat.exceptions.PrivateChatNotFoundException;
import bingol.campus.chat.request.DeleteMessageRequest;
import bingol.campus.chat.request.EditMessageRequest;
import bingol.campus.chat.request.SendMessageRequest;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PrivateChatService {
    DataResponseMessage sendPrivateMessage(String username, SendMessageRequest sendMessageRequest) throws StudentNotFoundException, PrivateChatNotFoundException;

    ResponseMessage createChat(String username, String username1) throws StudentNotFoundException;

    DataResponseMessage getChats(String username) throws StudentNotFoundException;

    DataResponseMessage getMessages(String username, UUID chatId) throws StudentNotFoundException, PrivateChatNotFoundException;

    DataResponseMessage<Map<String, Object>> getUserStatus(String username) throws StudentNotFoundException;

    ResponseMessage editMessage(String username, EditMessageRequest editMessageRequest);

    ResponseMessage deleteMessage(String username, DeleteMessageRequest deleteMessageRequest);

    ResponseMessage deleteChat(String username, UUID chatId);

    DataResponseMessage<List<String>> getReadReceipts(String username, UUID messageId);
}
