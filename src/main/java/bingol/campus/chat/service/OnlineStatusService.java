package bingol.campus.chat.service;

import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.OnlineStatus;
import bingol.campus.chat.repository.OnlineStatusRepository;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineStatusService {
    private final StudentRepository studentRepository;
    private final OnlineStatusRepository onlineStatusRepository;

    public DataResponseMessage getUserStatus(String username, Long studentId) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        List<Chat> chats = student.getChats();
        boolean chatExists = chats.stream()
                .anyMatch(c -> c.getStudent1().getId().equals(studentId) || c.getStudent2().getId().equals(studentId));
        if (!chatExists) {
            return null;
        }
        return new DataResponseMessage("User status", true, onlineStatusRepository.findByStudentId(studentId));
    }

    public void updateUserStatus(Long studentId, boolean isOnline) throws StudentNotFoundException {
        OnlineStatus userStatus = onlineStatusRepository.findByStudentId(studentId);
        Student student = studentRepository.findById(studentId).orElseThrow(StudentNotFoundException::new);
        if (userStatus == null) {
            userStatus = new OnlineStatus();
            userStatus.setStudent(student);
        }
        userStatus.setOnline(isOnline);
        userStatus.setLastSeen(LocalDateTime.now());
        onlineStatusRepository.save(userStatus);
    }

    public DataResponseMessage lastSeen(String username, String username1) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        Student student1 = studentRepository.getByUserNumber(username1);
        List<Chat> chats = student.getChats();
        boolean chatExists = chats.stream()
                .anyMatch(c -> c.getStudent1().getId().equals(student1.getId()) || c.getStudent2().getId().equals(student1.getId()));
        if (!chatExists) {
            return null;
        }
        OnlineStatus onlineStatus = onlineStatusRepository.findByStudentId(student1.getId());
        return new DataResponseMessage("Last seen", true, onlineStatus.getLastSeen());


    }
}