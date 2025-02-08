package bingol.campus.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("PRIVATE")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PrivateChat extends Chat {

    private String chatPhoto; // Sohbetin fotoğrafı (Karşıdaki kullanıcının profil fotoğrafı olabilir)

    private String chatName; // Sohbet adı (Karşıdaki kullanıcının adı olabilir)
}
