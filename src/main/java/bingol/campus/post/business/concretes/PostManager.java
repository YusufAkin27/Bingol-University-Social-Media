package bingol.campus.post.business.concretes;

import bingol.campus.blockRelation.entity.BlockRelation;
import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.entity.Comment;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.followRelation.entity.FollowRelation;
import bingol.campus.like.core.converter.LikeConverter;
import bingol.campus.like.entity.Like;
import bingol.campus.like.repository.LikeRepository;
import bingol.campus.notification.NotificationController;
import bingol.campus.notification.SendBulkNotificationRequest;
import bingol.campus.notification.SendNotificationRequest;
import bingol.campus.post.business.abstracts.PostService;
import bingol.campus.post.core.converter.PostConverter;
import bingol.campus.post.core.exceptions.*;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.post.core.response.LikeDetailsDTO;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.post.entity.Post;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.security.exception.UserNotFoundException;
import bingol.campus.story.core.exceptions.OwnerStoryException;
import bingol.campus.story.core.exceptions.StoryNotFoundException;
import bingol.campus.story.entity.Story;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostManager implements PostService {
    private final PostConverter postConverter;
    private final PostRepository postRepository;
    private final StudentRepository studentRepository;
    private final CommentConverter commentConverter;
    private final StoryRepository storyRepository;
    private final LikeConverter likeConverter;
    private final LikeRepository likeRepository;
    private final NotificationController notificationController;
    private final CommentRepository commentRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public ResponseMessage add(String username, String description, String location, List<String> tagAPerson, MultipartFile[] photos) throws InvalidPostRequestException, StudentNotFoundException, UnauthorizedTaggingException, BlockedUserTaggedException, IOException {
        // Kullanıcıyı al
        Student student = studentRepository.getByUserNumber(username);

        // 1. Fotoğraf kontrolü: Fotoğraf boş olamaz
        if (photos == null || photos.length == 0) {
            return new ResponseMessage("Fotoğraf boş olamaz.", false);
        }

        // 2. Gönderi açıklaması kontrolü
        // Açıklama boş olabilir, bu yüzden burada kontrol yapmıyoruz

        // 3. Tag edilen kişiler kontrolü
        if (tagAPerson != null && !tagAPerson.isEmpty()) {
            validateTaggedPersons(tagAPerson, student);
        }

        // 4. Gönderiyi oluştur
        Post post = Post.builder()
                .description(description)  // Açıklama boş olabilir
                .location(location)
                .isActive(true)
                .isDelete(false)
                .photos(new ArrayList<>())  // Fotoğraflar için başlangıçta boş bir liste
                .taggedPersons(new ArrayList<>())
                .build();
        post.setStudent(student);  // Gönderiyi paylaşan öğrenciye ait bilgiyi set et

        // 5. Fotoğrafları Cloudinary'ye yükle
        for (MultipartFile photo : photos) {
            // Fotoğraf formatını kontrol et
            String contentType = photo.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return new ResponseMessage("Yalnızca JPEG veya PNG formatındaki dosyalar kabul edilir.", false);
            }

            // Maksimum dosya boyutu kontrolü (2MB)
            long maxFileSize = 10 * 1024 * 1024; // 2MB
            if (photo.getSize() > maxFileSize) {
                return new ResponseMessage("Dosya boyutu 10MB'den büyük olamaz.", false);
            }


            Map<String, String> uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
            String photoUrl = uploadResult.get("url");

            // Yüklenen fotoğrafın URL'sini post'a ekle
            post.getPhotos().add(photoUrl);  // Fotoğraf URL'sini post'un photos alanına ekle
        }

        // 6. Tag edilen kişileri ekle
        if (tagAPerson != null && !tagAPerson.isEmpty()) {
            for (String taggedUsername : tagAPerson) {
                Student taggedStudent = studentRepository.getByUserNumber(taggedUsername);
                if (taggedStudent != null) {
                    post.getTaggedPersons().add(taggedStudent);  // Taglenen öğrenciyi post'a ekle
                } else {
                    throw new StudentNotFoundException();
                }
            }
        }

        // 7. Öğrencinin gönderisini ekle
        student.getPost().add(post);

        // 8. Gönderiyi kaydet
        postRepository.save(post);

        List<String> fmcTokens = student.getFollowers().stream()
                .filter(f -> f.getFollowed().getFcmToken() != null && f.getFollowed().getIsActive())
                .map(f -> f.getFollowed().getFcmToken())
                .collect(Collectors.toList());

        if (!fmcTokens.isEmpty()) {
            SendBulkNotificationRequest sendBulkNotificationRequest = new SendBulkNotificationRequest();
            sendBulkNotificationRequest.setTitle("Yeni Gönderi");
            sendBulkNotificationRequest.setMessage(student.getUsername() + " kullanıcısı yeni gönderi paylaştı.");
            sendBulkNotificationRequest.setFmcTokens(fmcTokens);

            try {
                notificationController.sendToUsers(sendBulkNotificationRequest);
            } catch (Exception e) {
                System.err.println("Bildirim gönderme hatası: " + e.getMessage());
            }
        } else {
            System.out.println("Takipçiler arasında bildirim gönderilecek FCM token'ı bulunamadı.");
        }

        return new ResponseMessage("Gönderi başarıyla paylaşıldı.", true);
    }

    @Override
    @Transactional
    public ResponseMessage update(String username, Long postId, String description, String location, List<String> tagAPerson, MultipartFile[] photos) throws StudentNotFoundException, PostNotFoundException, PostNotFoundForUserException, IOException, UnauthorizedTaggingException, BlockedUserTaggedException {
        // Kullanıcıyı al
        Student student = studentRepository.getByUserNumber(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // Gönderi sahibinin doğrulanması
        if (!post.getStudent().equals(student)) {
            throw new PostNotFoundForUserException();
        }

        // Açıklama güncellemesi
        if (description != null) {
            post.setDescription(description);
        }

        // Konum güncellemesi
        if (location != null) {
            post.setLocation(location);
        }

        // Fotoğraflar güncellemesi
        if (photos != null && photos.length > 0) {
            List<String> updatedPhotos = new ArrayList<>();

            for (MultipartFile photo : photos) {
                // Fotoğraf formatını kontrol et
                String contentType = photo.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                    return new ResponseMessage("Yalnızca JPEG veya PNG formatındaki dosyalar kabul edilir.", false);
                }

                // Maksimum dosya boyutu kontrolü (2MB)
                long maxFileSize = 10 * 1024 * 1024; // 2MB
                if (photo.getSize() > maxFileSize) {
                    return new ResponseMessage("Dosya boyutu 10MB'den büyük olamaz.", false);
                }

                // Fotoğrafı Cloudinary'ye yükle
                Map<String, String> uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
                String photoUrl = uploadResult.get("url");

                // Yüklenen fotoğraf URL'sini ekle
                updatedPhotos.add(photoUrl);
            }

            // Güncellenen fotoğraf listesini set et
            post.setPhotos(updatedPhotos);
        }

        // Tag edilen kişiler güncellemesi
        if (tagAPerson != null && !tagAPerson.isEmpty()) {
            validateTaggedPersons(tagAPerson, student);

            List<Student> updatedTaggedPersons = new ArrayList<>();
            for (String taggedUsername : tagAPerson) {
                Student taggedStudent = studentRepository.getByUserNumber(taggedUsername);
                if (taggedStudent != null) {
                    updatedTaggedPersons.add(taggedStudent);
                } else {
                    throw new StudentNotFoundException();
                }
            }

            // Tag edilen kişileri güncelle
            post.setTaggedPersons(updatedTaggedPersons);
        }

        // Gönderiyi kaydet
        postRepository.save(post);

        return new ResponseMessage("Gönderi başarıyla güncellendi.", true);
    }


    private void validateTaggedPersons(List<String> taggedUsernames, Student student) throws StudentNotFoundException, BlockedUserTaggedException, UnauthorizedTaggingException {
        // Engellenen kullanıcıları listele
        Set<Student> blockedUsers = student.getBlocked().stream()
                .map(BlockRelation::getBlocked)
                .collect(Collectors.toSet());  // HashSet, O(1) zamanında arama sağlar

        // Geçerli kullanıcıları (takipçiler ve takip ettikleri) listele
        Set<Student> validUsers = new HashSet<>(student.getFollowing().stream()
                .map(FollowRelation::getFollowed)
                .collect(Collectors.toList()));
        validUsers.addAll(student.getFollowers().stream()
                .map(FollowRelation::getFollower)
                .toList());

        // Her taglenen kullanıcıyı kontrol et
        for (String taggedUsername : taggedUsernames) {
            Student taggedUser = studentRepository.getByUserNumber(taggedUsername);

            // Eğer tag edilen kullanıcı bulunamazsa, StudentNotFoundException fırlatılabilir
            if (taggedUser == null) {
                throw new StudentNotFoundException();
            }

            // 1. Engellenen kullanıcılar listesinde olup olmadığını kontrol et
            if (blockedUsers.contains(taggedUser)) {
                throw new BlockedUserTaggedException(taggedUsername);
            }

            // 2. Geçerli kullanıcılar listesinde olup olmadığını kontrol et
            if (!validUsers.contains(taggedUser)) {
                throw new UnauthorizedTaggingException(taggedUsername);
            }
        }
    }


    @Override
    @Transactional
    public ResponseMessage delete(String username, Long postId) throws PostNotFoundForUserException, UserNotFoundException, PostAlreadyDeleteException, PostAlreadyNotActiveException, StudentNotFoundException {
        // Kullanıcıyı al
        Student student = studentRepository.getByUserNumber(username);

        // 1. Gönderiyi doğrula
        Post post = student.getPost().stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst()
                .orElseThrow(PostNotFoundForUserException::new);

        // 2. Gönderinin zaten silinmiş olup olmadığını kontrol et
        if (Boolean.TRUE.equals(post.isDelete())) {
            throw new PostAlreadyDeleteException();
        }

        // 3. Gönderinin aktif olup olmadığını kontrol et
        if (!post.isActive()) {
            throw new PostAlreadyNotActiveException();
        }

        // 4. Gönderiyi sil
        post.setDelete(true);
        post.setActive(false);
        postRepository.save(post); // Değişiklikleri kaydet

        return new ResponseMessage("Gönderi başarıyla kaldırıldı: Gönderiniz başarıyla silindi.", true);
    }


    @Override
    public DataResponseMessage<PostDTO> getDetails(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostAccessDeniedWithBlockerException, PostAccessDeniedWithPrivateException {
        // Kullanıcıyı ve gönderiyi al
        Student student = studentRepository.getByUserNumber(username); // İstek yapan kullanıcı
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new); // Gönderiyi doğrula
        Student postOwner = post.getStudent(); // Gönderi sahibini al

        // 1. Engelleme kontrolü

        isBlockedByPostOwner(student, postOwner);
        // 2. Gizlilik ve takip kontrolü
        isPrivatePostOwner(student, postOwner);

        // 3. Gönderi detaylarını dönüştür ve döndür
        PostDTO postDTO = postConverter.toDto(post); // Gönderiyi DTO'ya dönüştür
        return new DataResponseMessage<>("Gönderi detayları başarıyla getirildi.", true, postDTO);
    }

    public boolean isBlockedByPostOwner(Student student, Student postOwner) throws PostAccessDeniedWithBlockerException {
        boolean isBlockedByPostOwner = postOwner.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocked().equals(student));
        boolean isBlockedByRequester = student.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocked().equals(postOwner));
        if (isBlockedByPostOwner || isBlockedByRequester) {
            throw new PostAccessDeniedWithBlockerException();
        }
        return true;
    }

    public boolean isPrivatePostOwner(Student student, Student postOwner) throws PostAccessDeniedWithPrivateException {
        if (postOwner.isPrivate()) {
            boolean isFollowing = postOwner.getFollowers().stream()
                    .anyMatch(followRelation -> followRelation.getFollower().equals(student));
            if (!isFollowing) {
                throw new PostAccessDeniedWithPrivateException();
            }
        }
        return true;
    }

    @Override
    public DataResponseMessage<List<PostDTO>> getMyPosts(String username, Pageable pageable) throws StudentNotFoundException {
        // Öğrenciyi bul
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının gönderdiği postları sayfalı şekilde al
        Page<Post> postsPage = postRepository.findByStudentAndIsActive(student, true, pageable);

        // Sayfa içeriğindeki gönderileri DTO'ya dönüştür
        List<PostDTO> postDTOS = postsPage.getContent().stream()
                .map(postConverter::toDto) // Postları DTO'ya dönüştür
                .collect(Collectors.toList());

        // Sayfa bilgisi ve içerik ile döndür
        return new DataResponseMessage<>("Başarılı", true, postDTOS);
    }


    @Override
    public DataResponseMessage<List<PostDTO>> getUserPosts(String username, String username1, Pageable pageable)
            throws PostAccessDeniedWithBlockerException, PostAccessDeniedWithPrivateException, StudentNotFoundException {

        // Öğrenciyi ve gönderi sahibini bul
        Student student = studentRepository.getByUserNumber(username);
        Student ownerPost = studentRepository.getByUserNumber(username1);

        // Kullanıcı kendi gönderilerini istiyorsa, getMyPosts metodunu çağır
        if (student.equals(ownerPost)) {
            return getMyPosts(student.getUsername(), pageable); // getMyPosts metoduna sayfalama parametresi ekle
        }

        // Kullanıcının, gönderi sahibini engelleyip engellemediğini kontrol et
        isBlockedByPostOwner(student, ownerPost);
        isBlockedByPostOwner(ownerPost, student);
        // Kullanıcının özel profilde olup olmadığını kontrol et
        isPrivatePostOwner(student, ownerPost);

        // Gönderilerin sayfalı şekilde alınması
        Page<Post> postsPage = postRepository.findByStudentAndIsActive(ownerPost, true, pageable);

        // Sayfa içeriğini DTO'ya dönüştür
        List<PostDTO> postDTOS = postsPage.getContent().stream()
                .map(postConverter::toDto)
                .collect(Collectors.toList());

        // Sayfa bilgisi ve içerik ile döndür
        return new DataResponseMessage<>("Gönderiler başarıyla alındı.", true, postDTOS);
    }

    @Override
    public ResponseMessage getLikeCount(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostAccessDeniedWithBlockerException, PostAccessDeniedWithPrivateException {
        Student student = studentRepository.getByUserNumber(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new); // Gönderiyi doğrula
        Student postOwner = post.getStudent(); // Gönderi sahibini al        return null;

        isBlockedByPostOwner(student, postOwner);
        isPrivatePostOwner(student, postOwner);

        return new ResponseMessage("" + post.getLikes().size(), true);

    }

    @Override
    public ResponseMessage getCommentCount(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostAccessDeniedWithBlockerException, PostAccessDeniedWithPrivateException {
        Student student = studentRepository.getByUserNumber(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new); // Gönderiyi doğrula
        Student postOwner = post.getStudent(); // Gönderi sahibini al        return null;

        isBlockedByPostOwner(student, postOwner);
        isPrivatePostOwner(student, postOwner);

        return new ResponseMessage("" + post.getComments().size(), true);
    }

    @Override
    public DataResponseMessage<List<LikeDetailsDTO>> getLikeDetails(
            String username, Long postId, Pageable pageable)
            throws StudentNotFoundException, PostNotFoundException, PostAccessDeniedWithPrivateException, PostAccessDeniedWithBlockerException {

        Student student = studentRepository.getByUserNumber(username);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Student postOwner = post.getStudent();

        isBlockedByPostOwner(student, postOwner);
        isBlockedByPostOwner(postOwner, student);
        isPrivatePostOwner(student, postOwner);

        Page<Like> likePage = likeRepository.findByPost(post, pageable);

        List<LikeDetailsDTO> likeDetailsDTOS = likePage.getContent().stream()
                .filter(like -> like.getStudent().getIsActive())
                .map(likeConverter::toDetails)
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Beğeni detayları başarıyla alındı.", true, likeDetailsDTOS);
    }

    @Override
    public DataResponseMessage<List<CommentDetailsDTO>> getCommentDetails(String username, Long postId, Pageable pageable) throws StudentNotFoundException, PostNotFoundException, PostAccessDeniedWithBlockerException, PostAccessDeniedWithPrivateException {
        Student student = studentRepository.getByUserNumber(username);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Student postOwner = post.getStudent();

        isBlockedByPostOwner(student, postOwner);
        isBlockedByPostOwner(postOwner, student);
        isPrivatePostOwner(student, postOwner);

        Page<Comment> commentPage = commentRepository.findByPost(post, pageable);

        List<CommentDetailsDTO> commentDetailsDTOS = commentPage.getContent().stream()
                .filter(comment -> comment.getStudent().getIsActive())
                .map(commentConverter::toDetails)
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Yorum detayları başarıyla alındı.", true, commentDetailsDTOS);
    }

    @Override
    public DataResponseMessage<List<LikeDetailsDTO>> getStoryLikeDetails(String username, Long storyId, Pageable pageRequest) throws StudentNotFoundException, OwnerStoryException, StoryNotFoundException, PostAccessDeniedWithPrivateException, PostAccessDeniedWithBlockerException {
        Student student = studentRepository.getByUserNumber(username);
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);
        Student student1 = story.getStudent();

        isBlockedByPostOwner(student, student1);
        isBlockedByPostOwner(student1, student);
        isPrivatePostOwner(student, student1);
        Page<Like> likes = likeRepository.findByStory(story, pageRequest);
        List<LikeDetailsDTO> likeDetailsDTOS = likes.stream().filter(like -> like.getStudent().getIsActive()).map(likeConverter::toDetails).toList();
        return new DataResponseMessage<>("hikaye beğenileri", true, likeDetailsDTOS);
    }

    @Override
    public DataResponseMessage<List<CommentDetailsDTO>> getStoryCommentDetails(String username, Long storyId, Pageable pageRequest) throws StudentNotFoundException, StoryNotFoundException, PostAccessDeniedWithBlockerException, PostAccessDeniedWithPrivateException {
        Student student = studentRepository.getByUserNumber(username);
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);
        Student student1 = story.getStudent();

        isBlockedByPostOwner(student, student1);
        isBlockedByPostOwner(student1, student);
        isPrivatePostOwner(student, student1);
        Page<Comment> comments = commentRepository.findByStory(story, pageRequest);
        List<CommentDetailsDTO> commentDetailsDTOS = comments.stream().filter(c -> c.getStudent().getIsActive()).map(commentConverter::toDetails).toList();
        return new DataResponseMessage<>("hikaye yorumları ", true, commentDetailsDTOS);
    }


}
