package bingol.campus.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("PRIVATE")
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class PrivateChat extends Chat {

    private String chatPhoto; // Sohbetin fotoğrafı (Karşıdaki kullanıcının profil fotoğrafı olabilir)

    private String chatName; // Sohbet adı (Karşıdaki kullanıcının adı olabilir)
}
