package bingol.campus.student.rules;

import bingol.campus.student.core.request.CreateStudentRequest;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.*;
import bingol.campus.student.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentRules {

    private final StudentRepository studentRepository;


    public void baseControl(Student student) throws StudentNotActiveException, StudentDeletedException {
        if (!student.getIsActive()){
            throw new StudentNotActiveException();
        }
        if (student.getIsDeleted()){
            throw new StudentDeletedException();
        }
    }


    public void validateUsername(String username) throws DuplicateUsernameException {

        if (studentRepository.existsByUserNumber(username)) {
            throw new DuplicateUsernameException();
        }
    }


    // Telefon numarası kontrolü (format ve benzersizlik)
    public void validateMobilePhone(String mobilePhone) throws InvalidMobilePhoneException, DuplicateMobilePhoneException {
        if (mobilePhone == null || !mobilePhone.matches("^(\\+\\d{1,3}|0)?\\d{10}$")) {
            throw new InvalidMobilePhoneException();
        }
        if (studentRepository.existsByMobilePhone(mobilePhone)) {
            throw new DuplicateMobilePhoneException();
        }
    }

    // Email kontrolü (format ve benzersizlik)
    public void validateEmail(String email) throws InvalidEmailException, DuplicateEmailException {
        // E-posta doğrulama: Genel format kontrolü ve bingol.edu.tr ile bitiş kontrolü
        if (email == null || !email.matches("^[\\w-\\.]+@bingol\\.edu\\.tr$")) {
            throw new InvalidEmailException();
        }
        // Benzersizlik kontrolü
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }


    // Diğer kontroller (zorunlu alanlar boş olmamalı)
    public void validateRequiredFields(CreateStudentRequest request) throws MissingRequiredFieldException {
        if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
            throw new MissingRequiredFieldException();
        }
        if (request.getLastName() == null || request.getLastName().isEmpty()) {
            throw new MissingRequiredFieldException();
        }
        if (request.getDepartment() == null) {
            throw new MissingRequiredFieldException();
        }
        if (request.getFaculty() == null) {
            throw new MissingRequiredFieldException();
        }
        if (request.getGrade() == null) {
            throw new MissingRequiredFieldException();
        }
        if (request.getBirthDate() == null) {
            throw new MissingRequiredFieldException();
        }
    }

    // Tüm kontrolleri çağıran yöntem
    public void validate(CreateStudentRequest request) throws InvalidSchoolNumberException, DuplicateUsernameException, InvalidMobilePhoneException, DuplicateMobilePhoneException, InvalidEmailException, DuplicateEmailException, MissingRequiredFieldException {
        validateUsername(request.getUsername());
        validateMobilePhone(request.getMobilePhone());
        validateEmail(request.getEmail());
        validateRequiredFields(request);
    }
}
