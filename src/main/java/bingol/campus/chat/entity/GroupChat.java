package bingol.campus.chat.entity;

import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@DiscriminatorValue("GROUP")
@NoArgsConstructor
@AllArgsConstructor
public class GroupChat extends Chat {

    @ManyToMany
    @JoinTable(
            name = "group_chat_admins",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_id")
    )
    private List<Student> admins; // Grup yöneticileri

    private String groupName; // Grup sohbeti adı

    private String groupPhotoUrl; // Grup sohbeti fotoğrafı
}
