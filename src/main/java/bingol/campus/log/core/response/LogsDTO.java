package bingol.campus.log.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogsDTO {
    private Long logId;
    private String message;
    private String sentAt;
}
