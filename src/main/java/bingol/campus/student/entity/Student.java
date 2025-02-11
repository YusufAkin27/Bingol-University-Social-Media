package bingol.campus.student.entity;


import bingol.campus.blockRelation.entity.BlockRelation;

import bingol.campus.chat.entity.ChatMedia;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.comment.entity.Comment;
import bingol.campus.followRelation.entity.FollowRelation;
import bingol.campus.friendRequest.entity.FriendRequest;
import bingol.campus.like.entity.Like;
import bingol.campus.log.entity.Log;
import bingol.campus.verificationToken.VerificationToken;

import bingol.campus.post.entity.Post;
import bingol.campus.story.entity.FeaturedStory;
import bingol.campus.story.entity.Story;
import bingol.campus.student.entity.enums.Department;
import bingol.campus.student.entity.enums.Faculty;
import bingol.campus.student.entity.enums.Grade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import bingol.campus.security.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"followers", "following", "blocked", "receiverRequest", "sentRequest", "post", "stories", "likes", "comments", "chatParticipants", "messages", "mediaFiles", "logs"})
@ToString(exclude = {"followers", "following", "blocked", "receiverRequest", "sentRequest", "post", "stories", "likes", "comments", "chatParticipants", "messages", "mediaFiles", "logs"})
@Table(name = "students")
public class Student extends User {

    private String firstName;
    private String lastName;
    private String email;
    private String mobilePhone;
    private String username;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private Faculty faculty;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    private LocalDate birthDate;
    private String profilePhoto;
    private Boolean gender;
    private Boolean isActive;
    private Boolean isDeleted;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    private boolean isPrivate;
    private String bio;
    private int popularityScore;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "followed", fetch = FetchType.LAZY)
    private List<FollowRelation> followers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "follower", fetch = FetchType.LAZY)
    private List<FollowRelation> following = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blocker", fetch = FetchType.LAZY)
    private List<BlockRelation> blocked = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FriendRequest> receiverRequest = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FriendRequest> sentRequest = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> post = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> recorded = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Story> stories = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeaturedStory> featuredStories = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Story> archivedStories = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> archivedPosts = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();
    // enum rozetler
    //Zaman Bazlı İçerik Temaları: Belirli dönemlerde (mevsim, tatil vb.) veya kullanıcının ilgi alanına göre otomatik değişen tema ve arayüz seçenekleri.

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatParticipant> chatParticipants = new ArrayList<>(); // Kullanıcının katıldığı sohbetler

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>(); // Kullanıcının gönderdiği mesajlar

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMedia> mediaFiles = new ArrayList<>(); // Kullanıcının gönderdiği medya dosyaları

    public int getPopularityScore() {
        return calculatePopularityScore();
    }

    private int calculatePopularityScore() {
        int followersCount = followers.size();
        int likesCount = likes.size();
        int commentsCount = comments.size();
        int postsCount = post.size();
        int storiesCount = stories.size();
        int featuredStoriesCount = featuredStories.size();

        return followersCount * 5 + likesCount * 2 + commentsCount + postsCount * 3 + storiesCount * 2 + featuredStoriesCount * 4;
    }


}
