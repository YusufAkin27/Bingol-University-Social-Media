package bingol.campus.story.business.concretes;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.entity.Comment;
import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.like.core.converter.LikeConverter;
import bingol.campus.like.entity.Like;
import bingol.campus.notification.NotificationController;
import bingol.campus.notification.SendBulkNotificationRequest;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.post.core.response.LikeDetailsDTO;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;

import bingol.campus.story.business.abstracts.StoryService;
import bingol.campus.story.core.exceptions.*;
import bingol.campus.story.core.converter.StoryConverter;
import bingol.campus.story.core.response.FeatureStoryDTO;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.core.response.StoryDetails;

import bingol.campus.story.entity.FeaturedStory;
import bingol.campus.story.entity.Story;

import bingol.campus.story.entity.StoryViewer;
import bingol.campus.story.repository.FeaturedStoryRepository;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.story.repository.StoryViewerRepository;
import bingol.campus.student.core.converter.StudentConverter;
import bingol.campus.student.core.response.SearchAccountDTO;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryManager implements StoryService {
    private final StudentRepository studentRepository;
    private final StoryRepository storyRepository;
    private final Cloudinary cloudinary;
    private final StoryConverter storyConverter;
    private final StudentConverter studentConverter;
    private final FeaturedStoryRepository featuredStoryRepository;
    private final CommentConverter commentConverter;
    private final StoryViewerRepository storyViewerRepository;
    private final LikeConverter likeConverter;
    private final NotificationController notificationController;

    @Override
    @Transactional
    public ResponseMessage add(String username, MultipartFile file) throws StudentNotFoundException, IOException {
        Student student = studentRepository.getByUserNumber(username);
        if (student == null) {
            throw new StudentNotFoundException();
        }

        Story story = new Story();
        story.setActive(true);
        story.setComments(new ArrayList<>());
        story.setLikes(new ArrayList<>());
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusDays(1));
        story.setStudent(student);

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();

            if (contentType == null) {
                return new ResponseMessage("Geçersiz dosya formatı.", false);
            }

            long maxFileSize = 50 * 1024 * 1024; // 50MB (Videolar için artırıldı)

            if (file.getSize() > maxFileSize) {
                return new ResponseMessage("Dosya boyutu 50MB'yi geçemez.", false);
            }

            Map<String, Object> uploadParams = ObjectUtils.emptyMap();
            Map uploadResult;

            if (contentType.startsWith("image/")) {
                if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                    return new ResponseMessage("Yalnızca JPEG ve PNG formatındaki resimler kabul edilir.", false);
                }
                uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                story.setPhoto((String) uploadResult.get("secure_url"));
            } else if (contentType.startsWith("video/")) {
                if (!contentType.equals("video/mp4") && !contentType.equals("video/quicktime")) {
                    return new ResponseMessage("Yalnızca MP4 ve QuickTime formatındaki videolar kabul edilir.", false);
                }
                uploadParams = ObjectUtils.asMap("resource_type", "video");
                uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                story.setPhoto((String) uploadResult.get("secure_url"));
            } else {
                return new ResponseMessage("Sadece resim veya video yükleyebilirsiniz.", false);
            }
        }

        student.getStories().add(story);
        storyRepository.save(story);
        studentRepository.save(student);

        List<String> fcmTokens = student.getFollowers().stream()
                .filter(f -> f.getFollowed().getFcmToken() != null && f.getFollowed().getIsActive())
                .map(f -> f.getFollowed().getFcmToken())
                .collect(Collectors.toList());

        if (!fcmTokens.isEmpty()) {
            SendBulkNotificationRequest sendBulkNotificationRequest = new SendBulkNotificationRequest();
            sendBulkNotificationRequest.setTitle("Yeni Hikaye");
            sendBulkNotificationRequest.setMessage(student.getUsername() + " yeni bir hikaye paylaştı.");
            sendBulkNotificationRequest.setFmcTokens(fcmTokens);

            try {
                notificationController.sendToUsers(sendBulkNotificationRequest);
            } catch (Exception e) {
                System.err.println("Bildirim gönderme hatası: " + e.getMessage());
            }
        } else {
            System.out.println("Takipçiler arasında bildirim gönderilecek FCM token'ı bulunamadı.");
        }

        return new ResponseMessage("Hikaye başarıyla eklendi.", true);
    }


    @Override
    @Transactional
    public ResponseMessage delete(String username, Long storyId)
            throws StoryNotFoundException, StudentNotFoundException, OwnerStoryException {
        Student student = studentRepository.getByUserNumber(username);
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);

        if (!student.getStories().contains(story)) {
            throw new OwnerStoryException();
        }
        student.getStories().remove(story);
        student.getArchivedStories().add(story);
        studentRepository.save(student);
        return new ResponseMessage("Hikaye arşive alındı.", true);
    }

    @Override
    public DataResponseMessage<StoryDetails> getStoryDetails(String username, Long storyId, Pageable pageable) throws StudentNotFoundException, StoryNotFoundException, OwnerStoryException {
        // Öğrenciyi buluyoruz
        Student student = studentRepository.getByUserNumber(username);

        // Hikayeyi buluyoruz
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);

        // Öğrencinin, hikaye sahibi olup olmadığını kontrol ediyoruz
        student.getStories().stream()
                .filter(story1 -> story1.equals(story))
                .findFirst()
                .orElseThrow(OwnerStoryException::new);

        StoryDetails storyDetails = storyConverter.toDetails(story, pageable);  // Sayfalama parametresini buraya ekliyoruz

        return new DataResponseMessage<>("Hikaye detayları başarıyla getirildi.", true, storyDetails);
    }

    @Override
    @Transactional
    public ResponseMessage featureUpdate(String username, Long featureId, String title, MultipartFile coverPhoto) throws StudentNotFoundException, FeaturedStoryGroupNotFoundException, FeaturedStoryGroupNotAccess, IOException {
        Student student = studentRepository.getByUserNumber(username);
        FeaturedStory featuredStory = featuredStoryRepository.findById(featureId).orElseThrow(FeaturedStoryGroupNotFoundException::new);
        if (!featuredStory.getStudent().equals(student)) {
            throw new FeaturedStoryGroupNotAccess();
        }
        if (title != null) {
            featuredStory.setTitle(title);
        }
        if (coverPhoto != null && !coverPhoto.isEmpty()) {
            String contentType = coverPhoto.getContentType();

            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png"))) {
                return new ResponseMessage("Yalnızca JPEG ve PNG formatındaki dosyalar kabul edilir.", false);
            }

            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (coverPhoto.getSize() > maxFileSize) {
                return new ResponseMessage("Dosya boyutu 10MB'yi geçemez.", false);
            }


            Map<String, Object> uploadParams = ObjectUtils.emptyMap(); // Parametreler olmadan doğrudan yükle

            Map uploadResult = cloudinary.uploader().upload(coverPhoto.getBytes(), uploadParams);
            String photoUrl = (String) uploadResult.get("secure_url");

            featuredStory.setCoverPhoto(photoUrl);
        }

        featuredStoryRepository.save(featuredStory);
        return new ResponseMessage("öne çıkarılan hikaye grubu düzenlendi", true);
    }

    @Override
    public DataResponseMessage<FeatureStoryDTO> getFeatureId(String username, Long featureId) throws StudentNotFoundException, FeaturedStoryGroupNotFoundException, BlockingBetweenStudent, StudentProfilePrivateException {
        Student student = studentRepository.getByUserNumber(username);
        FeaturedStory featuredStory = featuredStoryRepository.findById(featureId).orElseThrow(FeaturedStoryGroupNotFoundException::new);
        Student student1 = featuredStory.getStudent();
        accessStory(student, student1);
        FeatureStoryDTO featureStoryDTO = storyConverter.toFeatureStoryDto(featuredStory);

        return new DataResponseMessage<>("başarılı", true, featureStoryDTO);
    }

    @Override
    public DataResponseMessage<List<FeatureStoryDTO>> getFeaturedStoriesByStudent(String username, Long studentId) throws StudentNotFoundException, BlockingBetweenStudent, StudentProfilePrivateException {
        Student student = studentRepository.getByUserNumber(username);
        Student student1 = studentRepository.findById(studentId).orElseThrow(StudentNotFoundException::new);
        accessStory(student, student1);
        List<FeatureStoryDTO> featureStoryDTOS = student1.getFeaturedStories().stream().map(storyConverter::toFeatureStoryDto).toList();
        return new DataResponseMessage<>("başarılı", true, featureStoryDTOS);
    }

    @Override
    public DataResponseMessage<List<FeatureStoryDTO>> getMyFeaturedStories(String username) throws StudentNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        List<FeatureStoryDTO> featureStoryDTOS = student.getFeaturedStories().stream().map(storyConverter::toFeatureStoryDto).toList();
        return new DataResponseMessage<>("başarılı", true, featureStoryDTOS);
    }

    @Override
    public DataResponseMessage<List<StoryDTO>> archivedStories(String username) throws StudentNotFoundException {
        Student student=studentRepository.getByUserNumber(username);
        List<StoryDTO>storyDTOS=student.getArchivedStories().stream().map(storyConverter::toDto).toList();
        return new DataResponseMessage<>("arşiv",true,storyDTOS);
    }

    private void accessStory(Student student, Student student1) throws BlockingBetweenStudent, StudentProfilePrivateException {

        if (student.equals(student1)) {
            return;
        }
        boolean blocked = student.getBlocked().stream().anyMatch(b -> b.getBlocked().equals(student1)) ||
                student1.getBlocked().stream().anyMatch(b -> b.getBlocked().equals(student));

        if (blocked) {
            throw new BlockingBetweenStudent();
        }

        if (student1.isPrivate()) {
            boolean isFollowing = student.getFollowing().stream()
                    .anyMatch(f -> f.getFollowed().equals(student1));

            if (!isFollowing) {
                throw new StudentProfilePrivateException();
            }

        }
    }


    @Override
    @Transactional
    public ResponseMessage featureStory(String username, Long storyId, Long featuredStoryId)
            throws StudentNotFoundException, StoryNotFoundException, OwnerStoryException, AlreadyFeaturedStoriesException, FeaturedStoryGroupNotFoundException {
        Student student = studentRepository.getByUserNumber(username);
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);

        if (!student.getStories().contains(story)) {
            throw new OwnerStoryException();
        }
        Optional<FeaturedStory> existingFeaturedGroup = featuredStoryRepository.findFeaturedStoryByStudentAndStory(student, story);
        if (existingFeaturedGroup.isPresent()) {
            throw new AlreadyFeaturedStoriesException();
        }

        FeaturedStory featuredStory;
        if (featuredStoryId != null) {
            featuredStory = featuredStoryRepository.findById(featuredStoryId)
                    .orElseThrow(FeaturedStoryGroupNotFoundException::new);
        } else {
            // Yeni bir FeaturedStory grubu oluştur
            featuredStory = new FeaturedStory();
            featuredStory.setStudent(student);
            featuredStory.setTitle("Yeni Öne Çıkan Hikayeler"); // Varsayılan başlık
            featuredStory.setCreateAt(LocalDateTime.now());
            featuredStory.setCoverPhoto(story.getPhoto()); // Öne çıkan hikayenin kapağı olarak hikayenin fotoğrafı
            featuredStory = featuredStoryRepository.save(featuredStory);
        }

        // Hikayeyi öne çıkan olarak işaretle ve gruba ekle
        story.setFeatured(true);  // Hikaye öne çıkan olarak işaretleniyor
        story.setActive(true);    // Hikaye aktif duruma getirilir
        story.setFeaturedStory(featuredStory);
        // Hikayeyi gruba ekle
        featuredStory.getStories().add(story);  // Hikayeyi FeaturedStory'nin listesine ekle

        // Hem hikayeyi hem de featuredStory'i kaydet
        storyRepository.save(story);  // Hikaye kaydediliyor
        featuredStoryRepository.save(featuredStory);  // FeaturedStory kaydediliyor

        return new ResponseMessage("Hikaye başarıyla öne çıkarılanlara eklendi.", true);
    }


    @Override
    public DataResponseMessage<List<StoryDetails>> getStories(String username, Pageable pageable) throws StudentNotFoundException {
        // Öğrenciyi kullanıcı adı ile buluyoruz
        Student student = studentRepository.getByUserNumber(username);

        // Öğrencinin aktif hikayelerini sayfalayarak alıyoruz
        Page<Story> storiesPage = storyRepository.findByStudentAndIsActive(student, true, pageable);

        // Sayfa içeriğini StoryDetails DTO'ya dönüştürüyoruz
        List<StoryDetails> storyDetails = storiesPage.getContent().stream()
                .map(story -> storyConverter.toDetails(story, pageable))  // Sayfalama parametresini ileterek dönüştürme
                .collect(Collectors.toList());

        // Sonuçları döndürüyoruz
        return new DataResponseMessage<>("Aktif hikayeler başarıyla getirildi.", true, storyDetails);
    }


    @Override
    @Transactional
    public ResponseMessage extendStoryDuration(String username, Long storyId, int hours)
            throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException, InvalidHourRangeException, FeaturedStoryModificationException {

        // Kullanıcıyı getir
        Student student = studentRepository.getByUserNumber(username);

        // Eğer saat aralığı geçersizse hata fırlat
        if (hours < 1 || hours > 24) {
            throw new InvalidHourRangeException();
        }
        // Kullanıcının hikayelerinden ilgili hikayeyi bul
        Story story = student.getStories().stream()
                .filter(s -> s.getId().equals(storyId))
                .findFirst()
                .orElseThrow(OwnerStoryException::new);

        // Eğer hikaye aktif değilse süre uzatma yapılamaz
        if (!story.isActive()) {
            throw new StoryNotActiveException();
        }

        // Eğer hikaye öne çıkarılmışsa süresi değiştirilemez
        if (story.isFeatured()) {
            throw new FeaturedStoryModificationException();
        }

        // Süreyi uzat
        story.setExpiresAt(story.getExpiresAt().plusHours(hours));

        // Veritabanına kaydet
        storyRepository.save(story);
        studentRepository.save(student);

        return new ResponseMessage("Hikaye süresi başarıyla uzatıldı.", true);
    }


    @Override
    public List<SearchAccountDTO> getStoryViewers(String username, Long storyId)
            throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException {

        // Kullanıcıyı getir
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının hikayelerinden ilgili hikayeyi bul
        Story story = student.getStories().stream()
                .filter(s -> s.getId().equals(storyId))
                .findFirst()
                .orElseThrow(OwnerStoryException::new);

        // Eğer hikaye aktif değilse hata fırlat
        if (!story.isActive()) {
            throw new StoryNotActiveException();
        }

        return story.getViewers().stream()
                .map(viewer -> studentConverter.toSearchAccountDTO(viewer.getStudent())) // Doğru map kullanımı
                .collect(Collectors.toList());
    }


    @Override
    public int getStoryViewCount(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException {
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının hikayelerinden ilgili hikayeyi bul
        Story story = student.getStories().stream()
                .filter(s -> s.getId().equals(storyId))
                .findFirst()
                .orElseThrow(OwnerStoryException::new);

        // Eğer hikaye aktif değilse hata fırlat
        if (!story.isActive()) {
            throw new StoryNotActiveException();
        }
        return story.getViewers().size();
    }

    @Override
    public DataResponseMessage<List<StoryDTO>> getPopularStories(String username) throws StudentNotFoundException {
        // Tüm aktif ve özel olmayan (isPrivate=false) hikayeleri getir
        List<Story> activeStories = storyRepository.findAll().stream()
                .filter(story -> story.isActive() && !story.getStudent().isPrivate())
                .toList();

        // Hikayelere puan hesaplamayı zaten başka bir yerde yapıyorsunuz, bu yüzden sıralama yapalım
        List<Story> sortedStories = activeStories.stream()
                .sorted(Comparator.comparingLong(Story::getScore).reversed()) // Skora göre sıralama, long türü için doğru comparator
                .limit(3) // En yüksek 3 hikaye
                .toList();

        // DTO formatına çevir
        List<StoryDTO> popularStories = sortedStories.stream()
                .map(storyConverter::toDto) // StoryConverter ile DTO dönüşümü
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Popüler hikayeler listelendi", true, popularStories);
    }


    @Override
    public DataResponseMessage<List<StoryDTO>> getUserActiveStories(String username, String username1)
            throws StudentNotFoundException, BlockingBetweenStudent, NotFollowingException {
        // Öğrenci (student) bilgisi alınıyor
        Student student = studentRepository.getByUserNumber(username);
        // Takip edilecek öğrenci (student1) bilgisi alınıyor
        Student student1 = studentRepository.getByUserNumber(username1);

        // Engellemeleri kontrol edelim: Eğer student1, student'i engellemişse veya student, student1'i engellemişse
        boolean isBlockedByStudent1 = student1.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocker().equals(student));  // student1 tarafından engellenmiş mi?
        boolean isBlockedByStudent = student.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocker().equals(student1));  // student tarafından engellenmiş mi?

        if (isBlockedByStudent1 || isBlockedByStudent) {
            throw new BlockingBetweenStudent();
        }

        // Kullanıcının (student) student1'i takip etmesi durumu kontrol ediliyor
        boolean isFollowing = student.getFollowing().stream()
                .anyMatch(followRelation -> followRelation.getFollower().equals(student1));  // student1 takip ediliyor mu?

        // Eğer student1'in profili gizli değilse veya student, student1'i takip ediyorsa hikayeleri görebilir
        if (!student1.isPrivate() || isFollowing) {
            // student1'in aktif hikayelerini al
            List<Story> stories = student1.getStories().stream().filter(Story::isActive).toList();

            // StoryDTO'ya dönüştür
            List<StoryDTO> storyDTOS = stories.stream().map(storyConverter::toDto).collect(Collectors.toList());

            return new DataResponseMessage<>("Hikayeler başarıyla listelendi", true, storyDTOS);
        } else {
            // Kullanıcı profil gizli ve takip etmiyor, erişim reddediliyor
            throw new NotFollowingException();
        }
    }


    @Override
    public DataResponseMessage<List<CommentDetailsDTO>> getStoryComments(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException {
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının hikayelerinden ilgili hikayeyi bul
        Story story = student.getStories().stream()
                .filter(s -> s.getId().equals(storyId))
                .findFirst()
                .orElseThrow(OwnerStoryException::new);

        // Eğer hikaye aktif değilse hata fırlat
        if (!story.isActive()) {
            throw new StoryNotActiveException();
        }
        List<Comment> comments = story.getComments();
        List<CommentDetailsDTO> commentDetailsDTOS = comments.stream().map(commentConverter::toDetails).toList();

        return new DataResponseMessage<>("yorumlar", true, commentDetailsDTOS);
    }

    @Override
    @Transactional
    public DataResponseMessage<StoryDTO> viewStory(String username, Long storyId)
            throws StoryNotFoundException, StoryNotActiveException, StudentNotFoundException, NotFollowingException, BlockingBetweenStudent {
        // Öğrenci bilgisi alınıyor
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

        // Hikayeyi daha önce görüntülemiş mi kontrol et
        boolean hasViewedBefore = story.getViewers().stream()
                .anyMatch(storyViewer -> storyViewer.getStudent().equals(student));  // Aynı öğrenci daha önce görüntüledi mi?

        if (!hasViewedBefore) {
            // Hikayeye yeni bir görüntüleyen ekle
            StoryViewer storyViewer = new StoryViewer();
            storyViewer.setViewedAt(LocalDateTime.now());
            storyViewer.setStudent(student);
            storyViewer.setStory(story);
            storyViewerRepository.save(storyViewer);  // Yeni görüntüleme bilgisi kaydediliyor
        }

        StoryDTO storyDTO = storyConverter.toDto(story);
        return new DataResponseMessage<>("İçerik başarıyla görüntülendi", true, storyDTO);
    }


    @Override
    public DataResponseMessage<List<LikeDetailsDTO>> getLike(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException {
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının hikayelerinden ilgili hikayeyi bul
        Story story = student.getStories().stream()
                .filter(s -> s.getId().equals(storyId))
                .findFirst()
                .orElseThrow(OwnerStoryException::new);

        // Eğer hikaye aktif değilse hata fırlat
        if (!story.isActive()) {
            throw new StoryNotActiveException();
        }
        List<Like> likes = story.getLikes();
        List<LikeDetailsDTO> likeDetailsDTOS = likes.stream().map(likeConverter::toDetails).toList();
        return new DataResponseMessage<>("beğenenler", true, likeDetailsDTOS);
    }


}
