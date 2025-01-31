package bingol.campus.story.core.exceptions;

import bingol.campus.security.exception.BusinessException;

public class NotFollowingException extends BusinessException {
    public NotFollowingException( ) {
        super("Hikayelere erişim için takip etmeniz gerekmektedir.");
    }
}
