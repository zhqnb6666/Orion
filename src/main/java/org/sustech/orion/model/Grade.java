package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "grader_id")
    private User grader;

    @Column(nullable = false)
    private Double score; // 分数

    @Column(columnDefinition = "TEXT")
    private String feedback; // 反馈意见

    @Column(nullable = false)
    private Timestamp gradedTime; // 评分时间

    @Column()
    private Boolean isFinalized; // 是否最终评分

    @Column(columnDefinition = "TEXT")
    private String appealReason;//新增 申诉理由

    @Column
    private Timestamp appealTime;//新增 申诉时间

    @Enumerated(EnumType.STRING)
    private Status status = Status.UPCOMING;

    @Getter
    public enum Status
    {
        GRADED, UPCOMING, SUBMITTED, MISSING, APPEALING, APPEALED;

        private final String value;

        Status() {
            this.value = this.name().toLowerCase();
        }
    }
}