package org.sustech.orion.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "plagiarism_checks")
public class PlagiarismCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "submission_a_id", nullable = false)
    private Submission submissionA;

    @ManyToOne
    @JoinColumn(name = "submission_b_id", nullable = false)
    private Submission submissionB;

    @Column(nullable = false)
    private Double similarityScore; // 相似度分数

    @Column(nullable = false)
    private Timestamp checkTime; // 检查时间

    @Column(nullable = false)
    private String status; // 检查状态

    @Column(columnDefinition = "TEXT")
    private String details; // 详细比对结果
}