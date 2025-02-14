package bingol.campus.chat.business.abstracts;

import bingol.campus.chat.exceptions.PrivateChatNotFoundException;
import bingol.campus.chat.request.SendMessageRequest;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.exceptions.StudentNotFoundException;

public interface PrivateChatService {
    DataResponseMessage sendPrivateMessage(String username, SendMessageRequest sendMessageRequest) throws StudentNotFoundException, PrivateChatNotFoundException;

    ResponseMessage createChat(String username, String username1) throws StudentNotFoundException;

    DataResponseMessage getChats(String username) throws StudentNotFoundException;

    DataResponseMessage getMessages(String username, Long chatId) throws StudentNotFoundException, PrivateChatNotFoundException;
}
