package bingol.campus.chat.core.exceptions;

import bingol.campus.security.exception.BusinessException;

public class MessageNotFoundException extends BusinessException {
    public MessageNotFoundException( ) {
        super("mesaj bulunamadı");
    }
}
