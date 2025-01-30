package bingol.campus.student.entity;


import bingol.campus.blockRelation.entity.BlockRelation;
import bingol.campus.chat.entity.ChatMedia;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.Message;
import bingol.campus.comment.entity.Comment;
import bingol.campus.followRelation.entity.FollowRelation;
import bingol.campus.friendRequest.entity.FriendRequest;
import bingol.campus.like.entity.Like;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"followers", "following", "blocked", "receiverRequest", "sentRequest", "post", "stories", "likes", "comments", "chatParticipants", "messages", "mediaFiles"})
@ToString(exclude = {"followers", "following", "blocked", "receiverRequest", "sentRequest", "post", "stories", "likes", "comments", "chatParticipants", "messages", "mediaFiles"})
@Table(name = "students") // Öğrenci için ayrı tablo
public class Student extends User {


    private String firstName;

    private String lastName;

    private String email;

    private String mobilePhone;

    private String username;

    @Enumerated(EnumType.STRING)
    private Department department; // Bölüm

    @Enumerated(EnumType.STRING)
    private Faculty faculty; // Fakülte

    @Enumerated(EnumType.STRING)
    private Grade grade; // Sınıf (1, 2, 3, 4)

    private LocalDate birthDate; // Doğum Tarihi

    private String profilePhoto; // Profil Fotoğrafı (Dosya Yolu veya URL)

    private Boolean gender; // Cinsiyet (true: Erkek, false: Kadın)

    private Boolean isActive = true; // Aktiflik Durumu

    private Boolean isDeleted = false; // Silinmiş Durumu


    // sosyal medya için gerekli alanlar
    private boolean isPrivate;  //profil gizli mi
    private String bio;
    private int popularityScore;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "followed", fetch = FetchType.LAZY)
    private List<FollowRelation> followers = new ArrayList<>(); // Beni takip edenlerin ilişkisi

    // Takip ettiklerim
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "follower", fetch = FetchType.LAZY)
    private List<FollowRelation> following = new ArrayList<>(); // Benim takip ettiklerim

    // Engellenenler ilişkisi
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blocker", fetch = FetchType.LAZY)
    private List<BlockRelation> blocked = new ArrayList<>();

    // Arkadaşlık istekleri
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FriendRequest> receiverRequest = new ArrayList<>(); // Alıcı olarak gelen istekler

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FriendRequest> sentRequest = new ArrayList<>(); // Gönderen olarak yapılan istekler


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Post> post = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Story> stories = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeaturedStory> featuredStories = new ArrayList<>(); // Kullanıcının öne çıkarılan hikaye grupları


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatParticipant> chatParticipants = new ArrayList<>(); // Kullanıcının katıldığı sohbetler

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>(); // Kullanıcının gönderdiği mesajlar

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMedia> mediaFiles = new ArrayList<>(); // Kullanıcının gönderdiği medya dosyaları
    public List<Story> getFeaturedStories() {
        return stories.stream().filter(Story::isFeatured).toList();
    }


}
