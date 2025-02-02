package bingol.campus.student.business.concretes;


import bingol.campus.followRelation.business.abstracts.FollowRelationService;
import bingol.campus.followRelation.entity.FollowRelation;
import bingol.campus.friendRequest.business.abstracts.FriendRequestService;
import bingol.campus.friendRequest.entity.FriendRequest;
import bingol.campus.friendRequest.core.exceptions.BlockedByUserException;
import bingol.campus.friendRequest.core.exceptions.UserBlockedException;
import bingol.campus.post.core.converter.PostConverter;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.post.entity.Post;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.security.entity.Role;
import bingol.campus.security.entity.User;
import bingol.campus.security.exception.UserNotFoundException;
import bingol.campus.security.repository.UserRepository;
import bingol.campus.story.core.converter.StoryConverter;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.entity.Story;
import bingol.campus.story.entity.StoryViewer;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.story.repository.StoryViewerRepository;
import bingol.campus.student.business.abstracts.StudentService;
import bingol.campus.student.core.converter.StudentConverter;
import bingol.campus.student.core.response.*;
import bingol.campus.student.core.request.CreateStudentRequest;
import bingol.campus.student.core.request.UpdateStudentProfileRequest;
import bingol.campus.student.entity.Student;
import bingol.campus.student.entity.enums.Department;
import bingol.campus.student.entity.enums.Faculty;
import bingol.campus.student.entity.enums.Grade;
import bingol.campus.student.exceptions.*;

import bingol.campus.student.repository.StudentRepository;
import bingol.campus.student.rules.StudentRules;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentManager implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentConverter studentConverter;
    private final StudentRules studentRules;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRelationService followRelationService;
    private final FriendRequestService friendRequestService;
    private final PostRepository postRepository;
    private final PostConverter postConverter;
    private final Cloudinary cloudinary;
    private final StoryViewerRepository storyViewerRepository;
    private final StoryRepository storyRepository;
    private final StoryConverter storyConverter;


    @Override
    @Transactional
    public ResponseMessage signUp(CreateStudentRequest createStudentRequest) throws DuplicateUsernameException, MissingRequiredFieldException,
            DuplicateMobilePhoneException, DuplicateEmailException, InvalidMobilePhoneException, InvalidSchoolNumberException, InvalidEmailException {
        // Doğrulama kurallarını çağırın
        studentRules.validate(createStudentRequest);

        // Doğrulamalardan geçen veriyi dönüştürün ve kaydedin
        Student student = studentConverter.createToStudent(createStudentRequest);
        studentRepository.save(student);


        // Başarılı yanıt oluşturun
        return new ResponseMessage("Kayıt başarılı", true);
    }


    public Student findBySchoolNumber(String schoolNumber) throws StudentNotFoundException {
        return Optional.ofNullable(studentRepository.getByUserNumber(schoolNumber))
                .orElseThrow(StudentNotFoundException::new);
    }


    public DataResponseMessage<StudentDTO> getStudentProfile(String username) throws StudentNotFoundException {
        Student student = findBySchoolNumber(username);  // studentRepository'in getByUserNumber metodunun çalıştığından emin olun.
        return new DataResponseMessage<>("başarılı", true, studentConverter.toDto(student));  // Burada generik tip olarak Student kullanıldı.
    }


    @Override
    @Transactional
    public ResponseMessage updateStudentProfile(String username, UpdateStudentProfileRequest updateRequest) throws StudentNotFoundException, StudentDeletedException, StudentNotActiveException {
        // Öğrenciyi kullanıcı adına göre bul
        Student student = findBySchoolNumber(username);


        // Gelen güncelleme isteğindeki değerleri kullanarak öğrenciyi güncelle
        if (updateRequest.getFirstName() != null) {
            student.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            student.setLastName(updateRequest.getLastName());
        }

        if (updateRequest.getMobilePhone() != null) {
            student.setMobilePhone(updateRequest.getMobilePhone());
        }


        if (updateRequest.getGender() != null) {
            student.setGender(updateRequest.getGender());
        }

        // Öğrenci verisini veritabanında güncelle
        studentRepository.save(student);

        // Başarılı mesaj döndür
        return new ResponseMessage("öğrenci bilgilerin güncellendi", true);
    }


    @Override
    @Transactional
    public ResponseMessage uploadProfilePhoto(String userName, MultipartFile photo) throws StudentNotFoundException, IOException, StudentDeletedException, StudentNotActiveException {
        // Müşteriyi bul
        Student student = findBySchoolNumber(userName);
        studentRules.baseControl(student);
        // Fotoğraf formatını kontrol et
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            return new ResponseMessage("Yalnızca JPEG veya PNG formatındaki dosyalar kabul edilir.", false);
        }

        // Maksimum dosya boyutu kontrolü (2MB)
        long maxFileSize = 2 * 1024 * 1024; // 2MB
        if (photo.getSize() > maxFileSize) {
            return new ResponseMessage("Dosya boyutu 2MB'den büyük olamaz.", false);
        }

        // Fotoğrafı Cloudinary'ye yükle
        Map<String, String> uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());

        // Yüklenen fotoğrafın URL'sini al
        String photoUrl = uploadResult.get("url");

        // Müşteri kaydına URL'yi ekle
        student.setProfilePhoto(photoUrl);
        studentRepository.save(student); // Veritabanında güncelle

        return new ResponseMessage("Profil fotoğrafı başarıyla yüklendi: " + photoUrl, true);
    }


    @Override
    @Transactional
    public ResponseMessage deleteStudent(String username) throws StudentNotFoundException, StudentAlreadyIsActiveException {
        // Öğrenciyi bul
        Student student = findBySchoolNumber(username);

        if (!student.getIsActive()) {
            throw new StudentAlreadyIsActiveException();
        }

        // Öğrenci durumunu pasif ve silinmiş olarak güncelle
        student.setIsActive(false);
        student.setIsDeleted(true);
        studentRepository.save(student);

        // Başarılı yanıt döndür
        return new ResponseMessage("Hesabınız silindi.", true);
    }


    @Override
    @Transactional
    public ResponseMessage updatePassword(String username, String newPassword) throws StudentNotFoundException, SamePasswordException, StudentDeletedException, StudentNotActiveException {
        // Öğrenciyi bul
        Student student = findBySchoolNumber(username);


        // Yeni şifre mevcut şifreyle aynıysa hata fırlat
        if (student.getPassword().equals(newPassword)) {
            throw new SamePasswordException();
        }

        // Şifreyi güncelle ve şifreyi güvenli şekilde şifrele
        student.setPassword(passwordEncoder.encode(newPassword));

        // Öğrenci bilgilerini kaydet
        studentRepository.save(student);

        // Başarılı yanıt döndür
        return new ResponseMessage("Şifre güncellendi", true);
    }


    @Override
    @Transactional
    public ResponseMessage updateStudentStatus(String username, Boolean isActive) throws StudentNotFoundException, StudentStatusAlreadySetException {

        // Öğrenci var mı kontrol et
        Student student = findBySchoolNumber(username);

        // Durum zaten istenen değerde mi kontrol et
        if (student.getIsActive().equals(isActive)) {
            throw new StudentStatusAlreadySetException();
        }

        student.setIsActive(isActive);
        studentRepository.save(student);

        String status = isActive ? "aktif" : "pasif";
        return new ResponseMessage("Öğrenci durumu başarıyla " + status + " olarak güncellendi.", true);
    }

    @Override

    public DataResponseMessage<List<StudentDTO>> getAllStudents(String username, int page, int size) throws UnauthorizedException, UserNotFoundException {
        // Kullanıcı kontrolü
        Optional<User> user = userRepository.findByUserNumber(username);

        // Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol et
        if (!user.get().getRoles().contains(Role.ADMIN)) {
            throw new UnauthorizedException();
        }

        // Tüm öğrencileri getir ve DTO'ya dönüştür
        List<Student> all = studentRepository.findAll();
        List<StudentDTO> studentDTOS = all.stream()
                .map(studentConverter::toDto)
                .toList();

        // Başarılı yanıt döndür
        return new DataResponseMessage<>("Listeleme başarılı", true, studentDTOS);
    }


    @Override
    public DataResponseMessage<Long> countStudentsByDepartmentOrFaculty(String username, Department department, Faculty faculty) throws UserNotFoundException, UnauthorizedException {
        // Kullanıcı kontrolü
        Optional<User> user = userRepository.findByUserNumber(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        // Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol et
        if (!user.get().getRoles().contains(Role.ADMIN)) {
            throw new UnauthorizedException();
        }

        // Departman veya fakülteye göre filtreleme
        long count = studentRepository.findAll()
                .stream()
                .filter(student -> department.equals(student.getDepartment()) ||
                        faculty.equals(student.getFaculty()))
                .count();

        // Yanıt döndür
        return new DataResponseMessage<>("İşlem başarılı: Öğrenciler sayıldı.", true, count);
    }


    @Override
    @Transactional
    public ResponseMessage deleteProfilePhoto(String username) throws StudentNotFoundException {
        Student student = findBySchoolNumber(username);
        student.setProfilePhoto("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQTvL0DPixth9rlG6S4ieYtwc98AH-7lEaRjN3PO2vtdSIcHTDPv58SC6XPc4dk2s0fhB4&usqp=CAU");
        return new ResponseMessage("profil fotoğrafı kaldırıldı", true);
    }

    @Override
    public DataResponseMessage<List<StudentDTO>> filterStudents(String username, LocalDate birthDate, Grade grade) throws UserNotFoundException, UnauthorizedException {
        // Kullanıcı kontrolü
        Optional<User> user = userRepository.findByUserNumber(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        // Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol et
        if (!user.get().getRoles().contains(Role.ADMIN)) {
            throw new UnauthorizedException();
        }

        // Doğum tarihi ve/veya sınıf filtresi
        List<StudentDTO> studentDTOS = studentRepository.findAll()
                .stream()
                .filter(student -> (birthDate == null || birthDate.equals(student.getBirthDate())) &&
                        (grade == null || grade.equals(student.getGrade())))
                .map(studentConverter::toDto) // Öğrencileri DTO'ya dönüştür
                .collect(Collectors.toList()); // Sonuçları listeye topla

        // Filtrelenmiş öğrenci listesi döndür
        return new DataResponseMessage<>("Öğrenciler başarıyla filtrelendi.", true, studentDTOS);
    }


    @Override
    public DataResponseMessage<List<StudentDTO>> getDeletedStudents(String username) throws UserNotFoundException, UnauthorizedException {
        // Kullanıcı kontrolü
        Optional<User> user = userRepository.findByUserNumber(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        // Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol et
        if (!user.get().getRoles().contains(Role.ADMIN)) {
            throw new UnauthorizedException();
        }
        // Doğum tarihi ve/veya sınıf filtresi
        List<StudentDTO> studentDTOS = studentRepository.findAll()
                .stream()
                .filter(Student::getIsDeleted)
                .map(studentConverter::toDto) // Öğrencileri DTO'ya dönüştür
                .toList(); // Sonuçları listeye topla
        return new DataResponseMessage<>("Silinmiş öğrenciler başarıyla getirildi.", true, studentDTOS);
    }

    @Override
    @Transactional
    public ResponseMessage restoreDeletedStudent(String username, Long studentId) throws InvalidOperationException, StudentNotFoundException, UnauthorizedException, UserNotFoundException {
        // Kullanıcı kontrolü
        Optional<User> user = userRepository.findByUserNumber(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        // Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol et
        if (!user.get().getRoles().contains(Role.ADMIN)) {
            throw new UnauthorizedException();
        }

        // Silinmiş öğrenciyi bul
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        if (studentOptional.isEmpty()) {
            throw new StudentNotFoundException();
        }

        Student student = studentOptional.get();

        // Öğrencinin silinmiş olup olmadığını kontrol et
        if (!student.getIsDeleted()) {
            throw new InvalidOperationException();
        }

        // Öğrenciyi geri getir (aktif yap)
        student.setIsDeleted(false);
        student.setIsActive(true);
        studentRepository.save(student);

        // Başarılı yanıt döndür
        return new ResponseMessage("Öğrenci başarıyla geri getirildi.", true);
    }


    @Override
    @Transactional
    public ResponseMessage updateAcademicInfo(String username, Department department, Faculty faculty) throws StudentNotFoundException, StudentNotActiveException, InvalidDepartmentException, InvalidFacultyException {
        // Öğrenciyi bul
        Student student = findBySchoolNumber(username);

        // Öğrencinin aktif olup olmadığını kontrol et
        if (!student.getIsActive()) {
            throw new StudentNotActiveException();
        }

        // Departman kontrolü: Geçerli bir departman olup olmadığını kontrol et
        if (department == null || department.getDisplayName().trim().isEmpty()) {
            throw new InvalidDepartmentException();
        }

        // Fakülte kontrolü: Geçerli bir fakülte olup olmadığını kontrol et
        if (faculty == null || faculty.getDisplayName().trim().isEmpty()) {
            throw new InvalidFacultyException();
        }

        // Öğrencinin akademik bilgilerini güncelle
        student.setDepartment(department);
        student.setFaculty(faculty);

        // Öğrenci bilgilerini kaydet
        studentRepository.save(student);

        // Başarılı yanıt döndür
        return new ResponseMessage("Akademik bilgiler başarıyla güncellendi.", true);
    }


    @Override
    public DataResponseMessage<StudentStatistics> getStudentStatistics(String username) throws UserNotFoundException, UnauthorizedException {
        // Kullanıcıyı kontrol et
        User user = userRepository.findByUserNumber(username)
                .orElseThrow(UserNotFoundException::new);

        // Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol et
        if (!user.getRoles().contains(Role.ADMIN)) {
            throw new UnauthorizedException();
        }

        // Öğrenciler verilerini al
        List<Student> students = studentRepository.findAll();

        // İstatistikleri oluştur
        long totalStudents = students.size();
        long activeStudents = students.stream().filter(Student::getIsActive).count();
        long inactiveStudents = students.stream().filter(student -> !student.getIsActive()).count();
        long deletedStudents = students.stream().filter(Student::getIsDeleted).count();

        // Departman dağılımı
        Map<String, Long> departmentDistribution = students.stream()
                .collect(Collectors.groupingBy(student -> student.getDepartment().getDisplayName(), Collectors.counting()));

        // Fakülte dağılımı
        Map<String, Long> facultyDistribution = students.stream()
                .collect(Collectors.groupingBy(student -> student.getFaculty().getDisplayName(), Collectors.counting()));

        // Cinsiyet dağılımı
        Map<String, Long> genderDistribution = students.stream()
                .collect(Collectors.groupingBy(student -> student.getGender() ? "Erkek" : "Kadın", Collectors.counting()));

        // Sınıf dağılımı
        Map<String, Long> gradeDistribution = students.stream()
                .collect(Collectors.groupingBy(student -> student.getGrade().toString(), Collectors.counting()));

        // İstatistikleri DTO'ya yerleştir
        StudentStatistics statistics = new StudentStatistics();
        statistics.setTotalStudents(totalStudents);
        statistics.setActiveStudents(activeStudents);
        statistics.setInactiveStudents(inactiveStudents);
        statistics.setDeletedStudents(deletedStudents);
        statistics.setDepartmentDistribution(departmentDistribution);
        statistics.setFacultyDistribution(facultyDistribution);
        statistics.setGenderDistribution(genderDistribution);
        statistics.setGradeDistribution(gradeDistribution);

        // Yanıtı döndür
        return new DataResponseMessage<>("Öğrenci istatistikleri başarıyla alındı.", true, statistics);
    }

    @Override
    @Transactional
    public ResponseMessage changePrivate(String username, boolean isPrivate) throws StudentNotFoundException, ProfileStatusAlreadySetException, StudentDeletedException, StudentNotActiveException {
        // Öğrenciyi al
        Student student = studentRepository.getByUserNumber(username);

        // Eğer profilin durumu zaten istenen durumdaysa hata fırlat
        if (student.isPrivate() == isPrivate) {
            throw new ProfileStatusAlreadySetException(isPrivate);
        }

        // Profil durumunu güncelle
        student.setPrivate(isPrivate);
        if (!isPrivate) {
            List<Long> friendRequests = student.getReceiverRequest().stream().map(FriendRequest::getId).toList();
            friendRequestService.acceptFriendRequestsBulk(username, friendRequests);
        }

        // Başarı mesajını döndür
        return new ResponseMessage("Profiliniz artık " + (isPrivate ? "kapalı" : "açık") + ".", true);
    }

    @Override
    public DataResponseMessage search(String username, String query, int page) throws StudentNotFoundException {
        // Öğrenciyi al
        Student student = studentRepository.getByUserNumber(username);

        // Engellenen ve engelleyen kullanıcıları al
        Set<Long> excludedUserIds = new HashSet<>();
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocked().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocker().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.add(student.getId()); // Kendisi de hariç tutulur

        // Sayfalama nesnesi oluştur
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Kullanıcıları repository üzerinden sorgula
        List<Student> matchingStudents = studentRepository.searchStudents(query, excludedUserIds, pageable);

        // Ortak takipçileri saymak için helper fonksiyonu oluştur
        Map<Student, Integer> studentCommonFollowersCount = new HashMap<>();
        for (Student matchedStudent : matchingStudents) {
            int commonFollowersCount = calculateCommonFollowers(student, matchedStudent);
            studentCommonFollowersCount.put(matchedStudent, commonFollowersCount);
        }

        // Ortak takipçi sayısına göre sıralama yap
        List<Student> sortedStudents = matchingStudents.stream()
                .sorted((s1, s2) -> Integer.compare(studentCommonFollowersCount.get(s2), studentCommonFollowersCount.get(s1)))  // Azalan sıralama
                .toList();

        // Sonuçları DTO'ya dönüştür
        List<SearchAccountDTO> searchAccountDTOS = sortedStudents.stream()
                .map(studentConverter::toSearchAccountDTO)
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Arama sonuçları", true, searchAccountDTOS);
    }

    // Ortak takipçileri hesaplayan yardımcı metod
    private int calculateCommonFollowers(Student student1, Student student2) {
        Set<String> student1Followers = student1.getFollowers().stream()
                .map(followRelation -> followRelation.getFollower().getUsername())
                .collect(Collectors.toSet());
        Set<String> student2Followers = student2.getFollowers().stream()
                .map(followRelation -> followRelation.getFollower().getUsername())
                .collect(Collectors.toSet());

        // Ortak takipçileri bul
        student1Followers.retainAll(student2Followers);
        return student1Followers.size(); // Ortak takipçi sayısını döndür
    }


    @Override
    public DataResponseMessage<List<PublicAccountDetails>> getStudentsByDepartment(String username, Department department, int page) throws StudentNotFoundException {
        // Öğrenciyi al
        Student student = studentRepository.getByUserNumber(username);

        // Sayfalama nesnesi oluştur
        int pageSize = 20;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Öğrencinin engellediği ve onu engelleyen kullanıcıları al
        Set<Long> excludedUserIds = new HashSet<>();
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocked().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocker().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.add(student.getId()); // Kendisi de hariç tutulur

        // Öğrencinin departmanına göre filtreleme
        Page<Student> studentsPage = studentRepository.findStudentsByDepartment(department, pageable);

        // Engellenenleri hariç tutma
        List<Student> filteredStudents = studentsPage.getContent().stream()
                .filter(s -> !excludedUserIds.contains(s.getId()))  // Engellenenleri hariç tut
                .toList();

        // Sonuçları DTO'ya dönüştür
        List<PublicAccountDetails> studentDTOs = filteredStudents.stream()
                .map(studentConverter::publicAccountDto)
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Departmana göre arama sonuçları", true, studentDTOs);
    }

    @Override
    public DataResponseMessage<List<PublicAccountDetails>> getStudentsByFaculty(String username, Faculty faculty, int page) throws StudentNotFoundException {
        // Öğrenciyi al
        Student student = studentRepository.getByUserNumber(username);

        // Sayfalama nesnesi oluştur
        int pageSize = 20;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Öğrencinin engellediği ve onu engelleyen kullanıcıları al
        Set<Long> excludedUserIds = new HashSet<>();
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocked().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocker().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.add(student.getId()); // Kendisi de hariç tutulur

        // Öğrencinin fakültesine göre filtreleme
        Page<Student> studentsPage = studentRepository.findStudentsByFaculty(faculty, pageable);

        // Engellenenleri hariç tutma
        List<Student> filteredStudents = studentsPage.getContent().stream()
                .filter(s -> !excludedUserIds.contains(s.getId()))  // Engellenenleri hariç tut
                .toList();

        // Sonuçları DTO'ya dönüştür
        List<PublicAccountDetails> studentDTOs = filteredStudents.stream()
                .map(studentConverter::publicAccountDto)
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Fakülteye göre arama sonuçları", true, studentDTOs);
    }

    @Override
    public DataResponseMessage<List<PublicAccountDetails>> getStudentsByGrade(String username, Grade grade, int page) throws StudentNotFoundException {
        // Öğrenciyi al
        Student student = studentRepository.getByUserNumber(username);

        // Sayfalama nesnesi oluştur
        int pageSize = 20;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Öğrencinin engellediği ve onu engelleyen kullanıcıları al
        Set<Long> excludedUserIds = new HashSet<>();
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocked().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.addAll(student.getBlocked().stream()
                .map(blockRelation -> blockRelation.getBlocker().getId())
                .collect(Collectors.toSet()));
        excludedUserIds.add(student.getId()); // Kendisi de hariç tutulur

        // Öğrencinin sınıfına göre filtreleme
        Page<Student> studentsPage = studentRepository.findStudentsByGrade(grade, pageable);

        // Engellenenleri hariç tutma
        List<Student> filteredStudents = studentsPage.getContent().stream()
                .filter(s -> !excludedUserIds.contains(s.getId()))  // Engellenenleri hariç tut
                .toList();

        // Sonuçları DTO'ya dönüştür
        List<PublicAccountDetails> studentDTOs = filteredStudents.stream()
                .map(studentConverter::publicAccountDto)
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Sınıfa göre arama sonuçları", true, studentDTOs);
    }

    @Override
    public DataResponseMessage<List<PublicAccountDetails>> getBestPopularity(String username) {
        // Tüm öğrencileri al
        List<Student> students = studentRepository.findAll();

        // Popülerlik skoruna göre sıralayıp ilk 3 öğrenciyi al
        List<PublicAccountDetails> topStudents = students.stream()
                .sorted(Comparator.comparingInt(Student::getPopularityScore).reversed()) // Azalan sırada sıralama
                .limit(3) // İlk 3 eleman
                .map(studentConverter::publicAccountDto) // Her birini DTO'ya dönüştür
                .collect(Collectors.toList());

        return new DataResponseMessage<>("Popülerlik sıralaması başarıyla alındı.", true, topStudents);
    }

    @Override
    public DataResponseMessage accountDetails(String username, Long userId) throws StudentNotFoundException, UserBlockedException, BlockedByUserException {
        // Kullanıcıyı al
        Student student = studentRepository.getByUserNumber(username);

        // Hedef kullanıcıyı al
        Student targetStudent = studentRepository.findById(userId)
                .orElseThrow(StudentNotFoundException::new);

        // Kullanıcıyı engelleyen kontrolü
        boolean isBlockedByTarget = targetStudent.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocked().equals(student));

        if (isBlockedByTarget) {
            throw new UserBlockedException();
        }

        // Kullanıcı hedef kullanıcıyı engellemiş mi kontrolü
        boolean hasBlockedTarget = student.getBlocked().stream()
                .anyMatch(blockRelation -> blockRelation.getBlocked().equals(targetStudent));

        if (hasBlockedTarget) {
            throw new BlockedByUserException();
        }

        // Ortak arkadaşlar

        DataResponseMessage<List<String>> dataResponseMessage = followRelationService.getCommonFollowers(username, targetStudent.getUsername());

        List<String> commonFriends = dataResponseMessage.getData();
        // Hedef kullanıcı gizli hesap mı?
        if (targetStudent.isPrivate()) {
            // Kullanıcı hedef kişiyi takip ediyor mu?
            boolean isFollowing = student.getFollowing().stream()
                    .anyMatch(followRelation -> followRelation.getFollowed().equals(targetStudent));

            // Private account details
            PrivateAccountDetails privateDetails = studentConverter.privateAccountDto(targetStudent);
            privateDetails.setFollow(isFollowing);  // Set follow status
            privateDetails.setCommonFriends(commonFriends);  // Set common friends
            return new DataResponseMessage("Hesap detayları başarıyla getirildi.", true, privateDetails);
        }

        // Public account details
        boolean isFollowing = student.getFollowing().stream()
                .anyMatch(followRelation -> followRelation.getFollowed().equals(targetStudent));

        PublicAccountDetails publicDetails = studentConverter.publicAccountDto(targetStudent);
        publicDetails.setFollow(isFollowing);  // Set follow status
        publicDetails.setCommonFriends(commonFriends);  // Set common friends
        return new DataResponseMessage("Hesap detayları başarıyla getirildi.", true, publicDetails);
    }

    @Override
    public DataResponseMessage<List<PostDTO>> getHomePosts(String username, int page) throws StudentNotFoundException {
        // Kullanıcıyı bul
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının takip ettiklerini al
        List<Student> followingList = student.getFollowing().stream()
                .map(FollowRelation::getFollowed)
                .toList();

        if (followingList.isEmpty()) {
            return new DataResponseMessage<>("Takip ettiğiniz kimse yok.", true, List.of());
        }

        // **Sayfalama ve sıralama için Pageable oluşturuyoruz**
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        // **Takip edilen kişilerin aktif ve silinmemiş gönderilerini çek**
        Page<Post> postPage = postRepository.findByStudentInAndIsActiveTrueAndIsDeleteFalse(followingList, pageable);

        // **Postları DTO'ya çevir**
        List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(postConverter::toDto)
                .toList();

        return new DataResponseMessage<>("Ana sayfa gönderileri başarıyla getirildi.", true, postDTOs);
    }


    @Override
    public DataResponseMessage<List<StoryDTO>> getHomeStories(String username, int page) throws StudentNotFoundException {
        // Kullanıcıyı bul
        Student student = studentRepository.getByUserNumber(username);

        // Kullanıcının takip ettiklerini al
        List<Student> followingList = student.getFollowing().stream()
                .map(FollowRelation::getFollowed)
                .toList();

        if (followingList.isEmpty()) {
            return new DataResponseMessage<>("Takip ettiğiniz kimsenin hikayesi bulunmuyor.", true, List.of());
        }

        // **Sayfalama ve sıralama için Pageable oluşturuyoruz**
        Pageable pageable = PageRequest.of(page, 10);

        // **Takip edilen kişilerin en güncel hikayelerini getir**
        Page<Story> storyPage = storyRepository.findByStudentInAndIsActiveTrueOrderByCreatedAtDesc(followingList, pageable);

        // Kullanıcının daha önce görüntülediği hikayeleri al
        List<StoryViewer> storyViewers = storyViewerRepository.findViewedStoryIdsByStudent(student);
        List<Long> ids = storyViewers.stream().map(StoryViewer::getId).toList();

        // **Sadece aktif olan hikayeleri al ve sıralama yap**
        List<Story> sortedStories = storyPage.getContent().stream()
                .filter(Story::isActive) // 🔥 SADECE AKTİF OLANLARI AL
                .sorted(Comparator
                        .comparing((Story s) -> ids.contains(s.getId())) // Görüntülenenleri en sona at
                        .thenComparing(Story::getCreatedAt, Comparator.reverseOrder()) // Yeni hikayeler önce gelsin
                )
                .toList();

        // Story'leri DTO'ya çevir
        List<StoryDTO> storyDTOs = sortedStories.stream()
                .map(storyConverter::toDto)
                .toList();

        return new DataResponseMessage<>("Ana sayfa hikayeleri başarıyla getirildi.", true, storyDTOs);
    }

    @Override
    @Transactional
    public ResponseMessage updateFcmToken(String username, String fcmToken) throws StudentNotFoundException {
        Student student = findBySchoolNumber(username);
        student.setFcmToken(fcmToken);
        studentRepository.save(student);
        return new ResponseMessage("başarılı", true);
    }


}