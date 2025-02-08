package bingol.campus.chat.core.exceptions;

import bingol.campus.security.exception.BusinessException;

public class MessageNotOwnerException extends BusinessException {
    public MessageNotOwnerException( ) {
        super("mesaj sahibi değilsiniz ");
    }
}
