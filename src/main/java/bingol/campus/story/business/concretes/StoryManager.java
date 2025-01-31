package bingol.campus.story.business.concretes;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.core.response.CommentDTO;
import bingol.campus.comment.entity.Comment;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.like.core.converter.LikeConverter;
import bingol.campus.like.entity.Like;
import bingol.campus.notification.NotificationController;
import bingol.campus.notification.SendBulkNotificationRequest;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.post.core.response.LikeDetailsDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;

import bingol.campus.story.business.abstracts.StoryService;
import bingol.campus.story.core.exceptions.*;
import bingol.campus.story.core.converter.StoryConverter;
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
    public ResponseMessage add(String username, MultipartFile photos) throws StudentNotFoundException, IOException {
        Student student = studentRepository.getByUserNumber(username);
        if (student == null) {
            throw new StudentNotFoundException();
        }

        Story story = new Story();
        story.setActive(true);
        story.setComments(new ArrayList<>());
        story.setLikes(new ArrayList<>());
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusDays(1)); // Bitiş tarihi şu an +1 gün olacak şekilde ayarlandı
        story.setStudent(student);

        // Fotoğraf yüklemesi
        if (photos != null && !photos.isEmpty()) {
            String contentType = photos.getContentType();

            // Dosya türünü kontrol et
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return new ResponseMessage("Yalnızca JPEG ve PNG formatındaki dosyalar kabul edilir.", false);
            }

            // Dosya boyutunu kontrol et (maksimum 10MB)
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (photos.getSize() > maxFileSize) {
                return new ResponseMessage("Dosya boyutu 10MB'yi geçemez.", false);
            }

            // Cloudinary'ye yükle
            Map<String, String> uploadResult = cloudinary.uploader().upload(photos.getBytes(), ObjectUtils.emptyMap());
            String photoUrl = uploadResult.get("url");

            story.setPhoto(photoUrl);
        }

        // Hikayeyi ve öğrenciyi kaydet
        student.getStories().add(story);
        storyRepository.save(story);
        studentRepository.save(student);

        List<String> fmcTokens = student.getFollowers().stream()
                .filter(f -> f.getFollowed().getFcmToken() != null && f.getFollowed().getIsActive())
                .map(f -> f.getFollowed().getFcmToken())
                .collect(Collectors.toList());

        if (!fmcTokens.isEmpty()) {
            SendBulkNotificationRequest sendBulkNotificationRequest = new SendBulkNotificationRequest();
            sendBulkNotificationRequest.setTitle("Yeni Hikaye");
            sendBulkNotificationRequest.setMessage(student.getUsername() + " kullanıcısı yeni hikaye paylaştı.");
            sendBulkNotificationRequest.setFmcTokens(fmcTokens);

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
    public ResponseMessage delete(String username, Long storyId) throws StoryNotFoundException, StudentNotFoundException, OwnerStoryException {
        Student student = studentRepository.getByUserNumber(username);
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);
        student.getStories().stream().filter(story1 -> story1.equals(story)).findFirst().orElseThrow(OwnerStoryException::new);
        student.getStories().removeIf(s -> s.getId().equals(storyId));
        storyRepository.delete(story);
        studentRepository.save(student);
        return new ResponseMessage("hikaye kaldırıldı", true);
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
    public ResponseMessage featureStory(String username, Long storyId, Long featuredStoryId)
            throws StudentNotFoundException, StoryNotFoundException, OwnerStoryException, AlreadyFeaturedStoriesException, FeaturedStoryGroupNotFoundException {

        // Öğrenciyi al
        Student student = studentRepository.getByUserNumber(username);

        // Hikayeyi al
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);

        // Kullanıcının hikayesi olup olmadığını kontrol et
        if (!student.getStories().contains(story)) {
            throw new OwnerStoryException();
        }

        // Hikaye zaten bir FeaturedStory grubunda yer alıyor mu?
        Optional<FeaturedStory> existingFeaturedGroup = featuredStoryRepository.findAllByStudent(student)
                .stream()
                .filter(fs -> fs.getStories().contains(story)) // Bu hikaye zaten mevcut grup içinde mi?
                .findFirst();

        if (existingFeaturedGroup.isPresent()) {
            throw new AlreadyFeaturedStoriesException();
        }

        // Öne çıkan hikaye grubunu bul ya da yeni oluştur
        FeaturedStory featuredStory;
        if (featuredStoryId != null) {
            featuredStory = featuredStoryRepository.findById(featuredStoryId)
                    .orElseThrow(() -> new FeaturedStoryGroupNotFoundException());
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
        student.getFeaturedStories().add(story);  // Öğrenciye ait featured stories listesine eklenir

        // Hikayeyi kaydet (Hikaye güncelleniyor)
        storyRepository.save(story);

        // Güncellenmiş featuredStory'yi kaydet
        featuredStoryRepository.save(featuredStory);

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
