package bingol.campus.like.business.concretes;

import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.like.business.abstracts.LikeService;
import bingol.campus.like.core.exceptions.AlreadyLikedException;
import bingol.campus.like.core.exceptions.PostNotFoundLikeException;
import bingol.campus.like.core.exceptions.StoryNotFoundLikeException;
import bingol.campus.like.entity.Like;
import bingol.campus.like.repository.LikeRepository;
import bingol.campus.notification.NotificationController;
import bingol.campus.notification.SendBulkNotificationRequest;
import bingol.campus.notification.SendNotificationRequest;
import bingol.campus.post.core.converter.PostConverter;
import bingol.campus.post.core.exceptions.PostNotFoundException;
import bingol.campus.post.core.exceptions.PostNotIsActiveException;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.post.entity.Post;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.story.core.converter.StoryConverter;
import bingol.campus.story.core.exceptions.NotFollowingException;
import bingol.campus.story.core.exceptions.StoryNotActiveException;
import bingol.campus.story.core.exceptions.StoryNotFoundException;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.entity.Story;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.student.core.converter.StudentConverter;
import bingol.campus.student.core.response.SearchAccountDTO;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeServiceManager implements LikeService {
    private final StudentRepository studentRepository;
    private final LikeRepository likeRepository;
    private final StoryRepository storyRepository;
    private final PostRepository postRepository;
    private final PostConverter postConverter;
    private final StoryConverter storyConverter;
    private final NotificationController notificationController;
    private final StudentConverter studentConverter;

    @Override
    @Transactional
    public ResponseMessage likeStory(String username, Long storyId) throws StoryNotFoundException, StudentNotFoundException, StoryNotActiveException, BlockingBetweenStudent, NotFollowingException, AlreadyLikedException {
        Student student = studentRepository.getByUserNumber(username);

        // Hikaye bilgisi alınıyor
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);

        // Hikayenin aktif olup olmadığını kontrol et
        if (!story.isActive()) {
            throw new StoryNotActiveException();
        }

        // Hikayeyi paylaştığı öğrenci (student1) bilgisi
        Student student1 = story.getStudent();

        // Engellemeleri kontrol et (student1 tarafından engellenmiş mi ve vice versa)
        boolean isBlockedByStudent1 = student1.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocker().equals(student));  // student1 tarafından engellenmiş mi?
        boolean isBlockedByStudent = student.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocker().equals(student1));  // student tarafından engellenmiş mi?

        if (isBlockedByStudent1 || isBlockedByStudent) {
            throw new BlockingBetweenStudent();  // Eğer engellenmişse, engellenmiş hatası fırlatılır
        }

        // Takip durumu kontrolü
        boolean isFollowing = student.getFollowing().stream()
                .anyMatch(followRelation -> followRelation.getFollower().equals(student1));  // student1 takip ediliyor mu?

        if (student1.isPrivate() && !isFollowing) {  // Profil gizli ve takip edilmiyorsa, erişim reddedilir
            throw new NotFollowingException();
        }
        // Beğeniyi kontrol et: Eğer kullanıcı bu hikayeyi zaten beğenmişse, hata fırlatılır
        boolean alreadyLiked = story.getLikes().stream()
                .anyMatch(like -> like.getStudent().equals(student));  // Kullanıcı daha önce beğendi mi?

        if (alreadyLiked) {
            return new ResponseMessage("zaten beğenildi", true);
        }

        // Yeni beğeni oluşturuluyor
        Like like = new Like();
        like.setLikedAt(LocalDateTime.now());
        like.setStory(story);
        like.setPost(null);
        like.setStudent(student);
        story.getLikes().add(like);

        // Veritabanına kaydediliyor
        likeRepository.save(like);
        storyRepository.save(story);
        studentRepository.save(student);

        if (student1.getFcmToken() != null) {
            SendNotificationRequest sendNotificationRequest = new SendNotificationRequest();
            sendNotificationRequest.setTitle("Hikayen beğenildi");
            sendNotificationRequest.setFmcToken(student1.getFcmToken());
            sendNotificationRequest.setMessage(student.getUsername() + " kullanıcısı hikayeni beğendi.");

            try {
                notificationController.sendToUser(sendNotificationRequest);
            } catch (Exception e) {
                System.err.println("Bildirim gönderme hatası: " + e.getMessage());
            }
        } else {
            System.out.println("Kabul edilen kullanıcının FCM Token değeri bulunamadı!");
        }

        return new ResponseMessage("Hikaye beğenildi", true);
    }

    @Override
    public ResponseMessage likePost(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostNotIsActiveException, BlockingBetweenStudent, NotFollowingException, AlreadyLikedException {
        Student student = studentRepository.getByUserNumber(username);

        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if (!post.isActive()) {
            throw new PostNotIsActiveException();
        }

        Student student1 = post.getStudent();

        boolean isBlockedByStudent1 = student1.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocker().equals(student));  // student1 tarafından engellenmiş mi?
        boolean isBlockedByStudent = student.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocker().equals(student1));  // student tarafından engellenmiş mi?

        if (isBlockedByStudent1 || isBlockedByStudent) {
            throw new BlockingBetweenStudent();
        }

        boolean isFollowing = student.getFollowing().stream()
                .anyMatch(followRelation -> followRelation.getFollower().equals(student1));  // student1 takip ediliyor mu?

        if (student1.isPrivate() && !isFollowing) {  // Profil gizli ve takip edilmiyorsa, erişim reddedilir
            throw new NotFollowingException();
        }
        boolean alreadyLiked = post.getLikes().stream()
                .anyMatch(like -> like.getStudent().equals(student));  // Kullanıcı daha önce beğendi mi?

        if (alreadyLiked) {
            return new ResponseMessage("zaten beğenildi", true);
        }
        Like like = new Like();
        like.setLikedAt(LocalDateTime.now());
        like.setStory(null);
        like.setPost(post);
        like.setStudent(student);
        post.getLikes().add(like);
        likeRepository.save(like);
        postRepository.save(post);
        studentRepository.save(student);


        if (student1.getFcmToken() != null) {
            SendNotificationRequest sendNotificationRequest = new SendNotificationRequest();
            sendNotificationRequest.setTitle("Gönderin beğenildi");
            sendNotificationRequest.setFmcToken(student1.getFcmToken());
            sendNotificationRequest.setMessage(student.getUsername() + " kullanıcısı gönderini  beğendi.");

            try {
                notificationController.sendToUser(sendNotificationRequest);
            } catch (Exception e) {
                System.err.println("Bildirim gönderme hatası: " + e.getMessage());
            }
        } else {
            System.out.println("Kabul edilen kullanıcının FCM Token değeri bulunamadı!");
        }

        return new ResponseMessage("Gönderi beğenildi", true);

    }

    @Override
    @Transactional
    public ResponseMessage unlikeStory(String username, Long storyId) throws StoryNotFoundException, StudentNotFoundException, StoryNotFoundLikeException {
        // Kullanıcıyı (student) bul
        Student student = studentRepository.getByUserNumber(username);


        // Hikayeyi bul
        Story story = storyRepository.findById(storyId)
                .orElseThrow(StoryNotFoundException::new);

        // Kullanıcının bu hikayeye yaptığı beğeniyi bul
        Like like = story.getLikes().stream()
                .filter(s -> s.getStudent().equals(student))
                .findFirst()
                .orElseThrow(StoryNotFoundLikeException::new);

        // Beğeniyi hikayeden kaldır ve veritabanından sil
        story.getLikes().remove(like);
        likeRepository.delete(like);

        // Başarı mesajı döndür
        return new ResponseMessage("Beğeni kaldırıldı: Kullanıcı " + username + " hikaye ID " + storyId + " beğenisini kaldırdı.", true);
    }


    @Override
    @Transactional
    public ResponseMessage unlikePost(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostNotFoundLikeException {
        // Kullanıcıyı (student) bul
        Student student = studentRepository.getByUserNumber(username);

        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        Like like = post.getLikes().stream()
                .filter(s -> s.getStudent().equals(student))
                .findFirst()
                .orElseThrow(PostNotFoundLikeException::new);

        post.getLikes().remove(like);
        likeRepository.delete(like);
        return new ResponseMessage("Beğeni kaldırıldı: Kullanıcı " + username + " Post ID " + postId + " beğenisini kaldırdı.", true);

    }

    @Override
    public DataResponseMessage<List<StoryDTO>> getUserLikedStories(String username) throws StudentNotFoundException {
        // Kullanıcıyı (student) bul
        Student student = studentRepository.getByUserNumber(username);


        // Kullanıcının beğendiği hikayeleri al
        List<Like> likes = student.getLikes();

        // Kullanıcının hiç beğenisi yoksa boş liste döndür
        if (likes == null || likes.isEmpty()) {
            return new DataResponseMessage<>("Kullanıcının beğendiği hikaye bulunamadı.", true, Collections.emptyList());
        }

        // Beğenilerin bağlı olduğu hikayeleri DTO formatına dönüştür
        List<StoryDTO> storyDTOS = likes.stream()
                .map(Like::getStory) // Her beğeniden hikayeyi al
                .filter(Objects::nonNull) // null olanları filtrele
                .map(storyConverter::toDto) // DTO'ya çevir
                .toList();

        return new DataResponseMessage<>("Beğendiğiniz hikayeler başarıyla getirildi.", true, storyDTOS);
    }


    @Override
    public DataResponseMessage<List<PostDTO>> getUserLikedPosts(String username) throws StudentNotFoundException {
        // Kullanıcıyı (student) bul
        Student student = studentRepository.getByUserNumber(username);


        // Kullanıcının beğendiği gönderileri al
        List<Like> likes = student.getLikes();

        // Kullanıcının hiç beğenisi yoksa boş liste döndür
        if (likes == null || likes.isEmpty()) {
            return new DataResponseMessage<>("Kullanıcının beğendiği gönderi bulunamadı.", true, Collections.emptyList());
        }

        // Post alanı null olmayan beğenileri filtrele
        List<PostDTO> postDTOS = likes.stream()
                .filter(like -> like != null && like.getPost() != null) // Like objesi ve post'u null olmayanları al
                .map(like -> postConverter.toDto(like.getPost())) // DTO'ya çevir
                .toList();

        // Eğer hiç geçerli gönderi yoksa boş liste döndür
        if (postDTOS.isEmpty()) {
            return new DataResponseMessage<>("Beğendiğiniz geçerli bir gönderi bulunamadı.", true, Collections.emptyList());
        }

        return new DataResponseMessage<>("Beğendiğiniz gönderiler başarıyla getirildi.", true, postDTOS);
    }

    @Override
    public DataResponseMessage<List<PostDTO>> getPostLikesAfter(Long postId, String dateTime) throws PostNotFoundException, DateTimeParseException {
        // Gönderiyi (post) bul
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // String olarak gelen tarihi LocalDateTime'e çevir
        LocalDateTime parsedDateTime;
        try {
            parsedDateTime = LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("Geçersiz tarih formatı: " + dateTime, dateTime, 0);
        }

        // Belirtilen tarihten sonra yapılan beğenileri al
        List<Like> likes = likeRepository.findByPostAndLikedAtAfter(post, parsedDateTime);

        // Eğer beğeni yoksa boş liste döndür
        if (likes.isEmpty()) {
            return new DataResponseMessage<>("Belirtilen tarihten sonra beğeni bulunamadı.", true, Collections.emptyList());
        }

        // Beğenilerin bağlı olduğu postları DTO formatına dönüştür
        List<PostDTO> postDTOS = likes.stream()
                .map(Like::getPost) // Like nesnesinden post'u al
                .filter(Objects::nonNull) // null olanları filtrele
                .map(postConverter::toDto) // DTO'ya çevir
                .toList();

        return new DataResponseMessage<>("Belirtilen tarihten sonra beğenilen gönderiler başarıyla getirildi.", true, postDTOS);
    }

    @Override
    public DataResponseMessage<SearchAccountDTO> searchUserInPostLikes(String username, Long postId, String targetUsername)
            throws PostNotFoundException, StudentNotFoundException, NotFollowingException {

        // Gönderiyi bul
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // Gönderi sahibi olan kullanıcıyı bul
        Student postOwner = post.getStudent();
        Student requester = studentRepository.getByUserNumber(username);

        // Kullanıcının gönderiye erişimi olup olmadığını kontrol et
        if (!hasAccessToContent(requester, postOwner)) {
            throw new NotFollowingException();
        }

        // Kullanıcının beğenilerini al ve hedef kullanıcıyı arat
        boolean isLiked = post.getLikes().stream()
                .anyMatch(like -> like.getStudent().getUserNumber().equals(targetUsername));

        if (!isLiked) {
            return new DataResponseMessage<>("Bu kullanıcı gönderinizi beğenmemiş.", true, null);
        }

        // Kullanıcı beğenmişse bilgilerini getir
        Student targetUser = studentRepository.getByUserNumber(targetUsername);
        SearchAccountDTO accountDTO = studentConverter.toSearchAccountDTO(targetUser);

        return new DataResponseMessage<>("Kullanıcı gönderinizi beğenmiş.", true, accountDTO);
    }

    @Override
    public DataResponseMessage<SearchAccountDTO> searchUserInStoryLikes(String username, Long storyId, String targetUsername)
            throws StudentNotFoundException, StoryNotFoundException, NotFollowingException {

        // Hikayeyi bul
        Story story = storyRepository.findById(storyId)
                .orElseThrow(StoryNotFoundException::new);

        // Hikaye sahibini ve istekte bulunan kullanıcıyı bul
        Student storyOwner = story.getStudent();
        Student requester = studentRepository.getByUserNumber(username);

        // Kullanıcının hikayeye erişimi olup olmadığını kontrol et
        if (!hasAccessToContent(requester, storyOwner)) {
            throw new NotFollowingException();
        }

        // Kullanıcının beğenilerini al ve hedef kullanıcıyı arat
        boolean isLiked = story.getLikes().stream()
                .anyMatch(like -> like.getStudent().getUserNumber().equals(targetUsername));

        if (!isLiked) {
            return new DataResponseMessage<>("Bu kullanıcı hikayenizi beğenmemiş.", true, null);
        }

        // Kullanıcı beğenmişse bilgilerini getir
        Student targetUser = studentRepository.getByUserNumber(targetUsername);
        SearchAccountDTO accountDTO = studentConverter.toSearchAccountDTO(targetUser);

        return new DataResponseMessage<>("Kullanıcı hikayenizi beğenmiş.", true, accountDTO);
    }

    private boolean hasAccessToContent(Student requester, Student contentOwner) {
        if (requester.equals(contentOwner)) {
            return true;
        }
        // Eğer içerik sahibi özel değilse, herkes erişebilir
        if (!contentOwner.isPrivate()) {
            return true;
        }

        // Eğer içerik sahibi ve isteyen kişi takipleşiyorsa erişime izin ver
        boolean isFollowing = contentOwner.getFollowers().stream()
                .anyMatch(follow -> follow.getFollower().equals(requester));

        return isFollowing;
    }


}
