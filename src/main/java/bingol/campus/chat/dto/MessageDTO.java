package bingol.campus.chat.dto;

import bingol.campus.chat.entity.Chat;
import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private long messageId;
    private String content;
    private long senderId;
    private long receiverId;
    private LocalDateTime sentAt;
    private long chatId;

    private LocalDateTime updatedAt;
    private Boolean isPinned;
    private Boolean isDeletedForSender;
    private Boolean isDeletedForAll;

}
