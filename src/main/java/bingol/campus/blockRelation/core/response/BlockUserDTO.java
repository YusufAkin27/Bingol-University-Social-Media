package bingol.campus.blockRelation.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockUserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime blockDate;
    private String profilePhoto;
}
