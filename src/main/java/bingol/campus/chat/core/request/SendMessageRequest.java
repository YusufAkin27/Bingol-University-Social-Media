package bingol.campus.chat.core.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private Long chatId; // Mevcut sohbet ID'si (Yeni oluşturuluyorsa null olabilir)

    private String username; // Yeni sohbet başlatılıyorsa alıcıyı belirler

    private String content; // Mesaj içeriği
}
