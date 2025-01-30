package bingol.campus.followRelation.entity;

import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private Student follower; // Takip eden öğrenci

    @ManyToOne
    @JoinColumn(name = "followed_id", nullable = false)
    private Student followed; // Takip edilen öğrenci

    private LocalDateTime followingDate = LocalDateTime.now(); // Takip edilme tarihi

}
