package bingol.campus.blockRelation.entity;

import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "block_relations")
public class BlockRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private Student blocker; // Engelleyen öğrenci

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private Student blocked; // Engellenen öğrenci

    private LocalDateTime blockDate = LocalDateTime.now(); // Engellemeyi gerçekleştiren tarih

}
