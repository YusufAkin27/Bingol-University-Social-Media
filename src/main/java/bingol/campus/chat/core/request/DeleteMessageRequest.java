package bingol.campus.chat.core.request;

import lombok.Data;

@Data
public class DeleteMessageRequest {
    private Long messageId;
    private boolean deleteForAll; // true: Herkesten sil, false: Sadece benden sil
}
