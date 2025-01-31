package bingol.campus.friendRequest.business.concretes;

import bingol.campus.followRelation.entity.FollowRelation;
import bingol.campus.followRelation.repository.FollowRelationRepository;
import bingol.campus.friendRequest.business.abstracts.FriendRequestService;
import bingol.campus.friendRequest.core.converter.FriendRequestConverter;
import bingol.campus.friendRequest.core.exceptions.*;
import bingol.campus.friendRequest.entity.FriendRequest;
import bingol.campus.friendRequest.entity.enums.RequestStatus;

import bingol.campus.friendRequest.repository.FriendRequestRepository;
import bingol.campus.friendRequest.core.response.ReceivedFriendRequestDTO;
import bingol.campus.friendRequest.core.response.SentFriendRequestDTO;
import bingol.campus.notification.NotificationController;
import bingol.campus.notification.SendNotificationRequest;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.student.entity.Student;
import bingol.campus.student.exceptions.StudentNotFoundException;
import bingol.campus.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRequestManager implements FriendRequestService {
    private final StudentRepository studentRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FollowRelationRepository followRelationRepository;
    private final FriendRequestConverter friendRequestConverter;
    private final NotificationController notificationController;

    @Override
    @Transactional
    public ResponseMessage sendFriendRequest(String username, Long userId) throws StudentNotFoundException, SelfFriendRequestException, AlreadySentRequestException, AlreadyFollowingException, BlockedByUserException, UserBlockedException {
        // Gönderen ve alıcıyı veritabanından al
        Student gönderen = studentRepository.getByUserNumber(username);
        Student alıcı = findById(userId);

        // Gönderen ve alıcının aynı kişi olmadığını kontrol et
        if (gönderen.getId().equals(alıcı.getId())) {
            throw new SelfFriendRequestException();
        }

        // Gönderenin alıcıyı takip edip etmediğini kontrol et
        boolean isFollowing = gönderen.getFollowing().stream()
                .anyMatch(followRelation -> followRelation.getFollowed().getId().equals(alıcı.getId()));

        // Eğer alıcıyı takip ediyorsa, arkadaşlık isteği gönderilemez
        if (isFollowing) {
            throw new AlreadyFollowingException();
        }

        // Gönderenin alıcıyı engellediğini kontrol et
        boolean isBlockedBySender = gönderen.getBlocked().stream()
                .anyMatch(blockedUser -> blockedUser.getId().equals(alıcı.getId()));

        // Eğer gönderen alıcıyı engellemişse, arkadaşlık isteği gönderilemez
        if (isBlockedBySender) {
            throw new BlockedByUserException();
        }

        // Alıcının göndereni engellediğini kontrol et
        boolean isBlockedByReceiver = alıcı.getBlocked().stream()
                .anyMatch(blockedUser -> blockedUser.getId().equals(gönderen.getId()));

        // Eğer alıcı göndereni engellemişse, arkadaşlık isteği gönderilemez
        if (isBlockedByReceiver) {
            throw new UserBlockedException();
        }

        // Daha önce arkadaşlık isteği gönderilmiş mi kontrol et
        boolean alreadyRequested = gönderen.getSentRequest().stream()
                .anyMatch(friendRequest -> friendRequest.getReceiver().getId().equals(alıcı.getId()));

        if (alreadyRequested) {
            throw new AlreadySentRequestException();
        }
        if (alıcı.isPrivate()) {
            // Yeni bir arkadaşlık isteği oluştur
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setReceiver(alıcı);
            friendRequest.setSender(gönderen);
            friendRequest.setSentAt(LocalDateTime.now());
            friendRequest.setStatus(RequestStatus.PENDING);

            alıcı.getReceiverRequest().add(friendRequest);
            gönderen.getSentRequest().add(friendRequest);
            friendRequestRepository.save(friendRequest);
            if (alıcı.getFcmToken() != null) {
                SendNotificationRequest sendNotificationRequest = new SendNotificationRequest();
                sendNotificationRequest.setTitle("İstek Geldi!!");
                sendNotificationRequest.setFmcToken(alıcı.getFcmToken());
                sendNotificationRequest.setMessage(gönderen.getUsername() + " Kullanıcısından istek geldi");

                try {
                    notificationController.sendToUser(sendNotificationRequest);
                } catch (Exception e) {
                    System.err.println("Bildirim gönderme hatası: " + e.getMessage());
                }
            } else {
                System.out.println("Kullanıcının FCM Token değeri bulunamadı!");
            }

            return new ResponseMessage("Arkadaşlık isteği başarıyla gönderildi.", true);

        } else {
            FollowRelation followRelation = new FollowRelation();
            followRelation.setFollower(gönderen);
            followRelation.setFollowed(alıcı);
            followRelation.setFollowingDate(LocalDateTime.now());

            // Her iki kullanıcının takip listelerini güncelle
            alıcı.getFollowers().add(followRelation);
            gönderen.getFollowing().add(followRelation);

            // Bildirim gönderme


            // Takip ilişkisini kaydet
            followRelationRepository.save(followRelation);

            // Kullanıcıları kaydet
            studentRepository.save(alıcı);
            studentRepository.save(gönderen);
        }


        // Başarılı mesaj döndür
        return new ResponseMessage("Arkadaşlık Eklendi", true);
    }


    public Student findById(Long userId) throws StudentNotFoundException {
        Optional<Student> student = studentRepository.findById(userId);
        if (student.isEmpty()) {
            throw new StudentNotFoundException();
        }
        return student.get();
    }


    @Override
    public DataResponseMessage<List<ReceivedFriendRequestDTO>> getReceivedFriendRequests(String username, Pageable pageable) throws StudentNotFoundException {
        // Öğrenciyi bul
        Student student = studentRepository.getByUserNumber(username);

        // Sayfa boyutunu 10 olarak ayarlıyoruz
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10);

        // Öğrencinin aldığı arkadaşlık isteklerini sayfalı şekilde al
        Page<FriendRequest> receivedRequestsPage = friendRequestRepository.findByReceiver(student, pageRequest);

        // Sayfa içeriğini DTO'ya dönüştür
        List<ReceivedFriendRequestDTO> receivedFriendRequestDTOS = receivedRequestsPage.getContent().stream()
                .filter(friendRequest -> friendRequest.getSender().getIsActive())
                .map(friendRequestConverter::receivedToDto) // DTO'ya dönüştür
                .collect(Collectors.toList());

        // Sayfa bilgisi ve içerik ile döndür
        return new DataResponseMessage<>("Gelen arkadaşlık istekleri başarıyla alındı.", true, receivedFriendRequestDTOS);
    }


    @Override
    public DataResponseMessage<List<SentFriendRequestDTO>> getSentFriendRequests(String username, Pageable pageable) throws StudentNotFoundException {
        // Öğrenciyi bul
        Student student = studentRepository.getByUserNumber(username);

        // Sayfa boyutunu 10 olarak ayarlıyoruz
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10);

        // Öğrencinin gönderdiği arkadaşlık isteklerini sayfalı şekilde al
        Page<FriendRequest> sentRequestsPage = friendRequestRepository.findBySender(student, pageRequest);

        // Sayfa içeriğini DTO'ya dönüştür
        List<SentFriendRequestDTO> sentFriendRequestDTOS = sentRequestsPage.getContent().stream()
                .filter(friendRequest -> friendRequest.getReceiver().getIsActive())// atkif kullanıcılar
                .map(friendRequestConverter::sentToDto) // DTO'ya dönüştür
                .collect(Collectors.toList());

        // Sayfa bilgisi ve içerik ile döndür
        return new DataResponseMessage("Gönderilen arkadaşlık istekleri başarıyla alındı.", true, sentFriendRequestDTOS);
    }


    @Override
    @Transactional
    public ResponseMessage acceptFriendRequest(String username, Long requestId)
            throws AlreadyAcceptedRequestException, FriendRequestNotFoundException,
            StudentNotFoundException, UnauthorizedRequestException {

        // Alıcıyı (isteği kabul eden kullanıcıyı) bul
        Student followed = studentRepository.getByUserNumber(username);


        // Arkadaşlık isteğini bul
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(FriendRequestNotFoundException::new);

        // Eğer isteği kabul eden kullanıcı, isteğin alıcısı değilse hata fırlat
        if (!friendRequest.getReceiver().equals(followed)) {
            throw new UnauthorizedRequestException();
        }

        // Eğer arkadaşlık isteği zaten kabul edildiyse hata fırlat
        if (friendRequest.getStatus() == RequestStatus.ACCEPTED) {
            throw new AlreadyAcceptedRequestException();
        }

        // Gönderen kullanıcıyı al
        Student follower = friendRequest.getSender();

        // Takip ilişkisini oluştur
        FollowRelation followRelation = new FollowRelation();
        followRelation.setFollower(follower);
        followRelation.setFollowed(followed);
        followRelation.setFollowingDate(LocalDateTime.now());

        // Her iki kullanıcının takip listelerini güncelle
        followed.getFollowers().add(followRelation);
        follower.getFollowing().add(followRelation);

        // Takip ilişkisini kaydet
        followRelationRepository.save(followRelation);

        // Arkadaşlık isteğini kaldır
        friendRequestRepository.delete(friendRequest);

        // Kullanıcıları kaydet
        studentRepository.save(follower);
        studentRepository.save(followed);

        if (followed.getFcmToken() != null) {
            SendNotificationRequest sendNotificationRequest = new SendNotificationRequest();
            sendNotificationRequest.setTitle("Arkadaşlık İsteği Kabul Edildi");
            sendNotificationRequest.setFmcToken(followed.getFcmToken());
            sendNotificationRequest.setMessage(follower.getUsername() + " kullanıcısı isteğini kabul etti.");

            try {
                notificationController.sendToUser(sendNotificationRequest);
            } catch (Exception e) {
                System.err.println("Bildirim gönderme hatası: " + e.getMessage());
            }
        } else {
            System.out.println("Kabul edilen kullanıcının FCM Token değeri bulunamadı!");
        }


        return new ResponseMessage("Arkadaşlık isteği başarıyla kabul edildi.", true);
    }


    @Override
    @Transactional
    public ResponseMessage rejectFriendRequest(String username, Long requestId) throws AlreadyRejectedRequestException, FriendRequestNotFoundException, StudentNotFoundException {
        // Alıcıyı bul
        Student student = studentRepository.getByUserNumber(username);


        // Arkadaşlık isteğini bul
        Optional<FriendRequest> optionalFriendRequest = friendRequestRepository.findById(requestId);
        if (optionalFriendRequest.isEmpty()) {
            throw new FriendRequestNotFoundException();
        }

        FriendRequest friendRequest = optionalFriendRequest.get();

        // Eğer arkadaşlık isteği zaten reddedildiyse, tekrar reddedilemez
        if (friendRequest.getStatus() == RequestStatus.REJECTED) {
            throw new AlreadyRejectedRequestException();
        }
        Student gönderen = friendRequest.getSender();
        // Arkadaşlık isteğini reddet
        friendRequest.setStatus(RequestStatus.REJECTED);

        // Gelen istekler listesinde değişiklik yap
        student.getReceiverRequest().remove(friendRequest);
        gönderen.getSentRequest().remove(friendRequest);

        // Gönderenin gönderilen istekler listesinde değişiklik yap
        friendRequest.getSender().getSentRequest().remove(friendRequest);

        // Liste değişikliklerini veritabanına kaydet
        studentRepository.save(student);
        friendRequestRepository.save(friendRequest);  // Yalnızca durumu güncelle ve kaydet, silme işlemi yapma

        return new ResponseMessage("Arkadaşlık isteği başarıyla reddedildi.", true);
    }


    @Override
    public DataResponseMessage getFriendRequestById(String username, Long requestId) throws UnauthorizedRequestException, FriendRequestNotFoundException, StudentNotFoundException {
        // Alıcıyı bul
        Student alıcı = studentRepository.getByUserNumber(username);


        // Arkadaşlık isteğini bul
        Optional<FriendRequest> optionalFriendRequest = friendRequestRepository.findById(requestId);
        if (optionalFriendRequest.isEmpty()) {
            throw new FriendRequestNotFoundException();
        }

        FriendRequest friendRequest = optionalFriendRequest.get();

        // Kullanıcının gönderdiği veya aldığı isteği kontrol et
        if (!friendRequest.getSender().getId().equals(alıcı.getId()) &&
                !friendRequest.getReceiver().getId().equals(alıcı.getId())) {
            throw new UnauthorizedRequestException();
        }

        // Arkadaşlık isteğini DTO'ya dönüştür
        ReceivedFriendRequestDTO receivedDTO = friendRequestConverter.receivedToDto(friendRequest);
        SentFriendRequestDTO sentDTO = friendRequestConverter.sentToDto(friendRequest);

        // İstek türüne göre DTO döndür
        if (friendRequest.getReceiver().getId().equals(alıcı.getId())) {
            return new DataResponseMessage("Gelen arkadaşlık isteği başarıyla getirildi.", true, receivedDTO);
        } else {
            return new DataResponseMessage("Gönderilen arkadaşlık isteği başarıyla getirildi.", true, sentDTO);
        }
    }

    @Override
    @Transactional
    public ResponseMessage cancelFriendRequest(String username, Long requestId) throws FriendRequestNotFoundException, UnauthorizedRequestException, StudentNotFoundException {
        // Alıcıyı bul
        Student student = studentRepository.getByUserNumber(username);


        // Arkadaşlık isteğini bul
        Optional<FriendRequest> optionalFriendRequest = friendRequestRepository.findById(requestId);
        if (optionalFriendRequest.isEmpty()) {
            throw new FriendRequestNotFoundException();
        }

        FriendRequest friendRequest = optionalFriendRequest.get();

        // Kullanıcının göndermiş olduğu isteği kontrol et
        if (!friendRequest.getSender().getId().equals(student.getId())) {
            throw new UnauthorizedRequestException();
        }
        student.getSentRequest().remove(friendRequest);
        friendRequest.getReceiver().getReceiverRequest().remove(friendRequest);

        // Veritabanında silme işlemi
        friendRequestRepository.delete(friendRequest);

        // Kullanıcı ve alıcı verilerini güncelleme
        studentRepository.save(student);
        student.getSentRequest().remove(friendRequest);
        friendRequest.getReceiver().getReceiverRequest().remove(friendRequest);
        friendRequestRepository.save(friendRequest);

        return new ResponseMessage("Arkadaşlık isteği başarıyla iptal edildi.", true);
    }


    @Override
    public ResponseMessage acceptFriendRequestsBulk(String username, List<Long> requestIds) throws StudentNotFoundException {
        // Kullanıcıyı al
        Student student = studentRepository.getByUserNumber(username);

        // İsteklerin durumlarını tutmak için listeler
        List<Long> acceptedRequests = new ArrayList<>();
        List<String> failedRequests = new ArrayList<>();

        for (Long requestId : requestIds) {
            try {
                // İsteği bul ve doğrula
                FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                        .orElseThrow(FriendRequestNotFoundException::new);

                // Eğer isteğin alıcısı bu kullanıcı değilse hata
                if (!friendRequest.getReceiver().equals(student)) {
                    throw new UnauthorizedRequestException();
                }

                // Eğer zaten kabul edilmişse hata
                if (friendRequest.getStatus().equals(RequestStatus.ACCEPTED)) {
                    throw new AlreadyAcceptedRequestException();
                }

                // İsteği onayla
                acceptFriendRequest(username, friendRequest.getId());

                // İsteği kaydet
                friendRequestRepository.save(friendRequest);
                acceptedRequests.add(requestId);
            } catch (Exception e) {
                failedRequests.add("Request ID: " + requestId + ", Error: " + e.getMessage());
            }
        }

        // Durumu döndür
        return new ResponseMessage(
                "Bulk accept completed. Accepted: " + acceptedRequests.size() + ", Failed: " + failedRequests.size(),
                true
        );
    }

    @Transactional
    @Override
    public ResponseMessage rejectFriendRequestsBulk(String username, List<Long> requestIds) throws StudentNotFoundException {
        // Kullanıcıyı al
        Student receiver = studentRepository.getByUserNumber(username);

        // Kullanıcının tüm gelen arkadaşlık isteklerini al
        List<Long> validRequestIds = receiver.getReceiverRequest()
                .stream()
                .map(FriendRequest::getId)
                .toList();

        // Gelen isteklerin tümünün kullanıcının gelen istek listesinde olup olmadığını kontrol et
        List<Long> invalidRequestIds = requestIds.stream()
                .filter(id -> !validRequestIds.contains(id))
                .toList();

        if (!invalidRequestIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid request IDs: " + invalidRequestIds);
        }

        // Geçerli istekleri reddet
        List<FriendRequest> requestsToReject = friendRequestRepository.findAllById(requestIds);
        for (FriendRequest request : requestsToReject) {
            request.setStatus(RequestStatus.REJECTED);
        }

        // Değişiklikleri kaydet
        friendRequestRepository.saveAll(requestsToReject);

        return new ResponseMessage("Successfully rejected " + requestsToReject.size() + " requests", true);
    }


}
