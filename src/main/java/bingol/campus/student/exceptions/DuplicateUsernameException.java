package bingol.campus.student.exceptions;

import bingol.campus.security.exception.BusinessException;

public class DuplicateUsernameException extends BusinessException {
    public DuplicateUsernameException( ) {
        super("Aynı kullanıcı adında başka bir öğrenci var");
    }
}
