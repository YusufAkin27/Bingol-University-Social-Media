package bingol.campus.friendRequest.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceivedFriendRequestDTO {
    private Long requestId;           // Arkadaşlık isteği ID'si
    private String senderPhotoUrl;    // Gönderenin profil fotoğrafının URL'si
    private String senderUsername;    // Gönderenin kullanıcı adı
    private String senderFullName;    // Gönderenin tam adı
    private String sentAt;            // İsteğin gönderildiği tarih/saat (formatlanmış)
    private long popularityScore;
}
