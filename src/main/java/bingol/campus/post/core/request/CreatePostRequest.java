package bingol.campus.post.core.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePostRequest {

    private List<MultipartFile> content;  // Birden fazla fotoğrafı tutacak liste

    private String description;
    private List<String> tagAPerson;
    private String location;
}
