package bingol.campus.friendRequest.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SentFriendRequestDTO {
    private Long requestId;            // Arkadaşlık isteği ID'si
    private String receiverPhotoUrl;   // Alıcının profil fotoğrafının URL'si
    private String receiverUsername;   // Alıcının kullanıcı adı
    private String receiverFullName;   // Alıcının tam adı
    private String sentAt;             // İsteğin gönderildiği tarih/saat (formatlanmış)
    private String status;             // İsteğin durumu (örn: PENDING, ACCEPTED, REJECTED)
    private long popularityScore;
}
