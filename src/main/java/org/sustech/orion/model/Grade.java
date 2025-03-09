package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "grader_id", nullable = false)
    private User grader;

    @Column(nullable = false)
    private Double score; // 分数

    @Column(columnDefinition = "TEXT")
    private String feedback; // 反馈意见

    @Column(nullable = false)
    private Timestamp gradedTime; // 评分时间

    @Column(nullable = false)
    private Boolean isFinalized; // 是否最终评分

    @Column(columnDefinition = "TEXT")
    private String appealReason;//新增 申诉理由

    @Column
    private Timestamp appealTime;//新增 申诉时间

    @Enumerated(EnumType.STRING)
    private Status status = Status.UPCOMING;

    public enum Status
    {
        GRADED, UPCOMING, SUBMITTED, MISSING, APPEALING, APPEALED
    }
}