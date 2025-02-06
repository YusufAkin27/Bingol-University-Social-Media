package bingol.campus.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Transformation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaUploadService {


    private final Cloudinary cloudinary;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;

    public String uploadAndOptimizeMedia(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IOException("Dosya formatı belirlenemedi.");
        }

        if (contentType.startsWith("image/")) {
            return uploadAndOptimizeImage(file);
        } else if (contentType.startsWith("video/")) {
            return uploadAndOptimizeVideo(file);
        } else {
            throw new IOException("Sadece fotoğraf ve video yüklenebilir.");
        }
    }

    private String uploadAndOptimizeImage(MultipartFile photo) throws IOException {
        if (photo.getSize() > MAX_IMAGE_SIZE) {
            throw new IOException("Fotoğraf boyutu 5MB'den büyük olamaz.");
        }

        Map<String, String> uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.asMap(
                "folder", "profile_photos",
                "quality", "auto:best",
                "format", "webp",
                "transformation", new Transformation()
                        .width(1280)
                        .height(1280)
                        .crop("limit")
        ));

        return uploadResult.get("url");
    }

    private String uploadAndOptimizeVideo(MultipartFile video) throws IOException {
        if (video.getSize() > MAX_VIDEO_SIZE) {
            throw new IOException("Video boyutu 50MB'den büyük olamaz.");
        }

        Map<String, String> uploadResult = cloudinary.uploader().upload(video.getBytes(), ObjectUtils.asMap(
                "folder", "profile_videos",
                "resource_type", "video",
                "format", "mp4",
                "quality", "auto",
                "transformation", new Transformation()
                        .width(1920)
                        .height(1080)
                        .crop("limit")
        ));

        return uploadResult.get("url");
    }
}
