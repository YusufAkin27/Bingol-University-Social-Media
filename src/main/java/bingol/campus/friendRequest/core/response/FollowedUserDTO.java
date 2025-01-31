package bingol.campus.friendRequest.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowedUserDTO {
    private long id;
    private String username;           // Kullanıcı adı
    private String fullName;           // Tam ad
    private String profilePhotoUrl;    // Profil fotoğrafı URL'si
    private LocalDateTime followedDate; // Takip edildiği tarih
    private boolean isActive;          // Takip ilişkisi aktif mi?
    private boolean isPrivate;
    private String bio;                // Kullanıcının biyografisi
    private int popularityScore;       // Kullanıcının popülerlik skoru
}
