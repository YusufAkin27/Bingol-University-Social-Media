package bingol.campus.story.controller;

import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.story.business.abstracts.StoryService;
import bingol.campus.story.core.exceptions.StoryNotActiveException;
import bingol.campus.story.core.exceptions.*;
import bingol.campus.story.core.response.FeatureStoryDTO;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.core.response.StoryDetails;
import bingol.campus.student.core.response.SearchAccountDTO;
import bingol.campus.student.exceptions.StudentNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    // Yeni hikaye ekleme
    @PostMapping("/add")
    public ResponseMessage add(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("photos") MultipartFile photos) throws StudentNotFoundException, IOException {
        return storyService.add(userDetails.getUsername(), photos);
    }

    // Mevcut bir hikayeyi silme
    @DeleteMapping("/{storyId}")
    public ResponseMessage delete(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long storyId) throws StoryNotFoundException, StudentNotFoundException, OwnerStoryException {
        return storyService.delete(userDetails.getUsername(), storyId);
    }


    @PutMapping("/{storyId}/feature")
    public ResponseMessage featureStory(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long storyId,
                                        @RequestParam(required = false) Long featuredStoryId)
            throws StoryNotFoundException, StudentNotFoundException, AlreadyFeaturedStoriesException, OwnerStoryException, FeaturedStoryGroupNotFoundException {

        String username = userDetails.getUsername();
        return storyService.featureStory(username, storyId, featuredStoryId);
    }

    @PutMapping("/{featureId}/update")
    public ResponseMessage featureUpdate(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long featureId,
                                         @RequestParam(required = false) String title,
                                         @RequestParam(required = false) MultipartFile coverPhoto) throws FeaturedStoryGroupNotFoundException, StudentNotFoundException, FeaturedStoryGroupNotAccess, IOException {
        return storyService.featureUpdate(userDetails.getUsername(),featureId,title,coverPhoto);
    }
    @GetMapping("/feature/{featureId}")
    public DataResponseMessage<FeatureStoryDTO> getFeatureId(@AuthenticationPrincipal UserDetails userDetails,
                                                             @PathVariable Long featureId) throws BlockingBetweenStudent, FeaturedStoryGroupNotFoundException, StudentNotFoundException, StudentProfilePrivateException {
        return storyService.getFeatureId(userDetails.getUsername(),featureId);
    }
    // Belirli bir öğrencinin öne çıkarılan hikayelerini getir
    @GetMapping("/student/{studentId}/featured-stories")
    public DataResponseMessage<List<FeatureStoryDTO>> getFeaturedStoriesByStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId)
            throws BlockingBetweenStudent, FeaturedStoryGroupNotFoundException, StudentNotFoundException, StudentProfilePrivateException {
        return storyService.getFeaturedStoriesByStudent(userDetails.getUsername(), studentId);
    }

    // Kullanıcının kendi öne çıkarılan hikayelerini getir
    @GetMapping("/me/featured-stories")
    public DataResponseMessage<List<FeatureStoryDTO>> getMyFeaturedStories(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        return storyService.getMyFeaturedStories(userDetails.getUsername());
    }


    // Tüm hikayeleri sayfalı olarak listeleme
    @GetMapping("/list")
    public DataResponseMessage<List<StoryDetails>> getStories(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) throws StudentNotFoundException {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10);
        return storyService.getStories(userDetails.getUsername(), pageRequest);
    }

    // Belirli bir hikaye detaylarını ve yorumlarını sayfalı olarak getirme
    @GetMapping("/{storyId}")
    public DataResponseMessage<StoryDetails> getStoryDetails(@AuthenticationPrincipal UserDetails userDetails,
                                                             @PathVariable Long storyId,
                                                             Pageable pageable) throws StoryNotFoundException, StudentNotFoundException, OwnerStoryException {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10);
        return storyService.getStoryDetails(userDetails.getUsername(), storyId, pageRequest);
    }


    // Belirli bir hikayenin süresini uzatma
    @PutMapping("/{storyId}/extend")
    public ResponseMessage extendStoryDuration(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long storyId,
                                               @RequestParam("hours") int hours) throws StoryNotActiveException, FeaturedStoryModificationException, StudentNotFoundException, OwnerStoryException, InvalidHourRangeException {
        return storyService.extendStoryDuration(userDetails.getUsername(), storyId, hours);
    }

    // Belirli bir hikayeyi görüntüleyen kullanıcıların listesi
    @GetMapping("/{storyId}/viewers")
    public List<SearchAccountDTO> getStoryViewers(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Long storyId) throws StoryNotActiveException, StudentNotFoundException, OwnerStoryException {
        return storyService.getStoryViewers(userDetails.getUsername(), storyId);
    }

    // Belirli bir hikayenin toplam görüntülenme sayısı
    @GetMapping("/{storyId}/views")
    public int getStoryViewCount(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long storyId) throws StoryNotActiveException, StudentNotFoundException, OwnerStoryException {
        return storyService.getStoryViewCount(userDetails.getUsername(), storyId);
    }

    // Popüler hikayeleri getirme
    @GetMapping("/popular")
    public DataResponseMessage<List<StoryDTO>> getPopularStories(@AuthenticationPrincipal UserDetails userDetails) throws StudentNotFoundException {
        return storyService.getPopularStories(userDetails.getUsername());
    }

    // Belirli bir kullanıcının aktif hikayelerini getirme
    @GetMapping("/user/{username}/active")
    public DataResponseMessage<List<StoryDTO>> getUserActiveStories(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String username) throws NotFollowingException, BlockingBetweenStudent, StudentNotFoundException {
        return storyService.getUserActiveStories(userDetails.getUsername(), username);
    }

    // Belirli bir hikayeye yapılan yorumları listeleme
    @GetMapping("/{storyId}/comments")
    public DataResponseMessage<List<CommentDetailsDTO>> getStoryComments(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @PathVariable Long storyId) throws StoryNotActiveException, StudentNotFoundException, OwnerStoryException {
        return storyService.getStoryComments(userDetails.getUsername(), storyId);
    }

    //görüntüle
    @PostMapping("/{storyId}/view")
    public DataResponseMessage<StoryDTO> viewStory(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long storyId)
            throws StoryNotFoundException, StudentNotFoundException, OwnerStoryException, NotFollowingException, StoryNotActiveException, BlockingBetweenStudent {
        return storyService.viewStory(userDetails.getUsername(), storyId);
    }

    @GetMapping("/{storyId}/getLike")
    public ResponseMessage getLike(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long storyId) throws StoryNotActiveException, StudentNotFoundException, OwnerStoryException {
        return storyService.getLike(userDetails.getUsername(), storyId);
    }

}
