package bingol.campus.student.exceptions;

import bingol.campus.security.exception.BusinessException;

public class InvalidEmailException extends BusinessException {
    public InvalidEmailException( ) {
        super("Geçersiz e-posta formatı.");
    }
}
