package bingol.campus.student.core.converter;

import bingol.campus.post.core.converter.PostConverter;
import bingol.campus.security.entity.Role;
import bingol.campus.story.entity.Story;
import bingol.campus.student.core.response.PrivateAccountDetails;
import bingol.campus.student.core.response.PublicAccountDetails;
import bingol.campus.student.core.request.CreateStudentRequest;
import bingol.campus.student.core.response.SearchAccountDTO;
import bingol.campus.student.core.response.StudentDTO;
import bingol.campus.student.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class StudentConverterImpl implements StudentConverter {
    private final PasswordEncoder passwordEncoder;
    private final PostConverter postConverter;


    @Override
    public Student createToStudent(CreateStudentRequest createStudentRequest) {

        Student student = new Student();
        student.setPrivate(false);
        student.setUserNumber(createStudentRequest.getUsername()); // kullanıcı adı
        student.setPassword(passwordEncoder.encode(createStudentRequest.getPassword())); // Şifreyi şifreliyoruz
        student.setRoles(Set.of(Role.STUDENT)); // Rolü belirliyoruz
        student.setGender(createStudentRequest.getGender()); // Cinsiyeti alıyoruz
        student.setEmail(createStudentRequest.getEmail()); // E-posta adresini alıyoruz
        student.setFaculty(createStudentRequest.getFaculty()); // Fakülteyi alıyoruz
        student.setBirthDate(createStudentRequest.getBirthDate()); // Doğum tarihini alıyoruz
        student.setIsDeleted(false); // Varsayılan olarak silinmiş değil
        student.setDepartment(createStudentRequest.getDepartment()); // Bölümü alıyoruz
        student.setFirstName(createStudentRequest.getFirstName()); // Adı alıyoruz
        student.setGrade(createStudentRequest.getGrade()); // Sınıfı alıyoruz
        student.setProfilePhoto("https://upload.wikimedia.org/wikipedia/commons/a/ac/Default_pfp.jpg");
        student.setMobilePhone(createStudentRequest.getMobilePhone()); // Telefonu alıyoruz
        student.setIsActive(true); // Varsayılan olarak aktif
        student.setUsername(createStudentRequest.getUsername());
        student.setLastName(createStudentRequest.getLastName()); // Soyadı alıyoruz
        return student;
    }


    @Override
    public StudentDTO toDto(Student student) {
        if (student == null) {
            return null;
        }

        StudentDTO studentDto = new StudentDTO();
        studentDto.setFirstName(student.getFirstName());
        studentDto.setLastName(student.getLastName());
        studentDto.setTcIdentityNumber(student.getUsername());
        studentDto.setEmail(student.getEmail());
        studentDto.setMobilePhone(student.getMobilePhone());
        studentDto.setUsername(student.getUsername());
        studentDto.setBirthDate(student.getBirthDate());
        studentDto.setGender(student.getGender());
        studentDto.setFaculty(student.getFaculty());
        studentDto.setDepartment(student.getDepartment());
        studentDto.setGrade(student.getGrade());
        studentDto.setProfilePhoto(student.getProfilePhoto());
        studentDto.setIsActive(student.getIsActive());
        studentDto.setIsDeleted(student.getIsDeleted());
        studentDto.setPrivate(student.isPrivate());
        studentDto.setBiography(student.getBio());
        studentDto.setPopularityScore(student.getPopularityScore());
        studentDto.setFollower(student.getFollowers() == null ? 0 : student.getFollowers().size());
        studentDto.setFollowing(student.getFollowing() == null ? 0 : student.getFollowing().size());
        studentDto.setBlock(student.getBlocked() == null ? 0 : student.getBlocked().size());
        studentDto.setComments(student.getComments() == null ? 0 : student.getComments().size());
        studentDto.setLikedContents(student.getLikes() == null ? 0 : student.getLikes().size());
        return studentDto;
    }

    @Override
    public PublicAccountDetails publicAccountDto(Student student) {
        PublicAccountDetails publicAccountDetails = PublicAccountDetails.builder()
                .id(student.getId())
                .username(student.getUsername())
                .fullName(student.getFirstName() + " " + student.getLastName())
                .profilePhoto(student.getProfilePhoto())
                .bio(student.getBio())
                .popularityScore(student.getPopularityScore())
                .isPrivate(student.isPrivate())
                .followerCount(student.getFollowers().size())
                .postCount(student.getPost().size())
                .followingCount(student.getFollowing().size())
                .featuredStories(student.getFeaturedStories().stream()
                        .map(Story::getPhoto)
                        .toList())
                .posts(student.getPost().stream()
                        .map(postConverter::toDto)
                        .toList())
                .stories(student.getStories().stream()
                        .map(Story::getPhoto)
                        .toList())
                .build();

        return publicAccountDetails;
    }

    @Override
    public PrivateAccountDetails privateAccountDto(Student student) {
        return PrivateAccountDetails.builder()
                .id(student.getId())
                .username(student.getUsername())
                .profilePhoto(student.getProfilePhoto())
                .bio(student.getBio())
                .followingCount(student.getFollowing().size())
                .followerCount(student.getFollowers().size())
                .postCount(student.getPost().size())
                .isPrivate(student.isPrivate())
                .popularityScore(student.getPopularityScore())
                .build();
    }

    @Override
    public SearchAccountDTO toSearchAccountDTO(Student student) {
        return SearchAccountDTO.builder()
                .fullName(student.getFirstName() + " " + student.getLastName())
                .id(student.getId())
                .profilePhoto(student.getProfilePhoto())
                .username(student.getUsername())
                .build();
    }


}
