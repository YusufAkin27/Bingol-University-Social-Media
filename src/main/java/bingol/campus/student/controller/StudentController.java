package bingol.campus.student.controller;


import bingol.campus.friendRequest.core.exceptions.BlockedByUserException;
import bingol.campus.friendRequest.core.exceptions.UserBlockedException;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;

import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.student.business.abstracts.StudentService;
import bingol.campus.student.core.response.PublicAccountDetails;
import bingol.campus.student.core.request.CreateStudentRequest;
import bingol.campus.student.core.response.StudentDTO;
import bingol.campus.student.core.request.UpdateStudentProfileRequest;
import bingol.campus.student.entity.enums.Department;
import bingol.campus.student.entity.enums.Faculty;
import bingol.campus.student.entity.enums.Grade;
import bingol.campus.student.exceptions.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/v1/api/student")
@RequiredArgsConstructor

public class StudentController {
    private final StudentService studentService;

    // Öğrenci kayıt olma
    @PostMapping("/sign-up")
    public ResponseMessage signUp(@RequestBody CreateStudentRequest createStudentRequest) throws DuplicateTcIdentityNumberException, DuplicateUsernameException, MissingRequiredFieldException, DuplicateMobilePhoneException, DuplicateEmailException, InvalidMobilePhoneException, InvalidSchoolNumberException, InvalidTcIdentityNumberException, InvalidEmailException {
        return studentService.signUp(createStudentRequest);
    }
    @PutMapping("/active")
    public ResponseMessage active(@RequestParam String token){
        return studentService.active(token);
    }


    @PostMapping("/forgot-password/{username}")
    public ResponseMessage forgotPassword(@PathVariable String username) throws StudentNotFoundException {
        return studentService.forgotPassword(username);
    }

    @PutMapping("/reset-password")
    public ResponseMessage resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        return studentService.resetPassword(token, newPassword);
    }

    // Öğrenci profil bilgilerini getirme
    @GetMapping("/profile")
    public DataResponseMessage<StudentDTO> getStudentProfile(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        return studentService.getStudentProfile(userDetails.getUsername());
    }

    // Profil bilgilerini güncelleme
    @PutMapping("/profile")
    public ResponseMessage updateStudentProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestBody UpdateStudentProfileRequest updateRequest) throws StudentNotFoundException, StudentDeletedException, StudentNotActiveException {
        return studentService.updateStudentProfile(userDetails.getUsername(), updateRequest);
    }

    // Profil fotoğrafı yükleme /profile-photo
    @PostMapping("/profile-photo")
    public ResponseMessage uploadPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile photo) throws IOException, StudentNotFoundException, StudentDeletedException, StudentNotActiveException {
        long maxFileSize = 24 * 1024 * 1024;
        if (photo.getSize() > maxFileSize) {
            return new ResponseMessage("Dosya boyutu 2MB'den büyük olamaz.", false);
        }
        return studentService.uploadProfilePhoto(userDetails.getUsername(), photo);
    }


    // Öğrenci hesap silme
    @DeleteMapping("/delete")
    public ResponseMessage deleteStudent(@AuthenticationPrincipal UserDetails userDetails) throws StudentAlreadyIsActiveException, StudentNotFoundException {
        return studentService.deleteStudent(userDetails.getUsername());
    }

    // Öğrenci şifresini güncelleme
    @PutMapping("/update-password")
    public ResponseMessage updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String newPassword) throws StudentInactiveException, SamePasswordException, StudentNotFoundException, StudentDeletedException, StudentNotActiveException {
        return studentService.updatePassword(userDetails.getUsername(), newPassword);
    }

    @PutMapping("/updateFcmToken")
    public ResponseMessage updateFmcToken(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String fcmToken) throws StudentNotFoundException {
        return studentService.updateFcmToken(userDetails.getUsername(), fcmToken);
    }

    // Profil fotoğrafını silme
    @DeleteMapping("/profile-photo")
    public ResponseMessage deleteProfilePhoto(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        // Giriş yapan öğrencinin profil fotoğrafını siler
        return studentService.deleteProfilePhoto(userDetails.getUsername());
    }

    @PutMapping("/change-private")
    public ResponseMessage changePrivate(@AuthenticationPrincipal UserDetails userDetails, @RequestParam boolean isPrivate) throws ProfileStatusAlreadySetException, StudentNotFoundException, StudentDeletedException, StudentNotActiveException {
        return studentService.changePrivate(userDetails.getUsername(), isPrivate);
    }

    @GetMapping("/search")
    public DataResponseMessage search(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page) throws StudentNotFoundException {
        return studentService.search(userDetails.getUsername(), query, page);
    }

    @GetMapping("/account-details/{userId}")
    public DataResponseMessage accountDetails(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long userId) throws StudentNotFoundException, UserBlockedException, BlockedByUserException {
        return studentService.accountDetails(userDetails.getUsername(), userId);
    }

    @GetMapping("/students-by-department")
    public DataResponseMessage<List<PublicAccountDetails>> getStudentsByDepartment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Department department,
            @RequestParam int page) throws StudentNotFoundException {
        return studentService.getStudentsByDepartment(userDetails.getUsername(), department, page);
    }

    @GetMapping("/students-by-faculty")
    public DataResponseMessage<List<PublicAccountDetails>> getStudentsByFaculty(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Faculty faculty,
            @RequestParam int page) throws StudentNotFoundException {
        return studentService.getStudentsByFaculty(userDetails.getUsername(), faculty, page);
    }

    @GetMapping("/students-by-grade")
    public DataResponseMessage<List<PublicAccountDetails>> getStudentsByGrade(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Grade grade,
            @RequestParam int page) throws StudentNotFoundException {
        return studentService.getStudentsByGrade(userDetails.getUsername(), grade, page);
    }

    @GetMapping("/best-popularity")
    public DataResponseMessage<List<PublicAccountDetails>> getBestPopularity(@AuthenticationPrincipal UserDetails userDetails) {
        return studentService.getBestPopularity(userDetails.getUsername());
    }

    //ana sayfadaki postlar
    @GetMapping("/home/posts")
    public DataResponseMessage<List<PostDTO>> getHomePosts(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestParam(defaultValue = "0") int page) throws StudentNotFoundException {
        return studentService.getHomePosts(userDetails.getUsername(), page);
    }

    // Ana sayfadaki storyleri listeleme
    @GetMapping("/home/stories")
    public DataResponseMessage<List<StoryDTO>> getHomeStories(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestParam(defaultValue = "0") int page) throws StudentNotFoundException {
        return studentService.getHomeStories(userDetails.getUsername(), page);
    }
}



