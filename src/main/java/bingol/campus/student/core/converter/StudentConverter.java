package bingol.campus.student.core.converter;

import bingol.campus.student.core.response.PrivateAccountDetails;
import bingol.campus.student.core.response.PublicAccountDetails;
import bingol.campus.student.core.request.CreateStudentRequest;
import bingol.campus.student.core.response.SearchAccountDTO;
import bingol.campus.student.core.response.StudentDTO;
import bingol.campus.student.entity.Student;

public interface StudentConverter {
    Student createToStudent(CreateStudentRequest createStudentRequest);
    StudentDTO toDto(Student student);
    PublicAccountDetails publicAccountDto(Student student);
    PrivateAccountDetails privateAccountDto(Student student);
    SearchAccountDTO toSearchAccountDTO(Student student);
}
