package bingol.campus.chat.core.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRequest {
    private List<Long> participantIds;  // Katılımcı ID'leri
    private String chatName;             // Grup adı (sadece grup sohbetleri için)
    private String chatPhotoUrl;         // Grup fotoğrafı URL'si (opsiyonel)
}
