package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "submission_statistics")
public class SubmissionStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private Integer totalSubmissions; // 总提交数

    @Column(nullable = false)
    private Double averageScore; // 平均分数

    @Column(nullable = false)
    private Double highestScore; // 最高分数

    @Column(nullable = false)
    private Double lowestScore; // 最低分数

    @Column(nullable = false)
    private Double medianScore; // 中位数分数

    @Column(columnDefinition = "TEXT")
    private String scoreDistribution; // 分数分布

    @Column(nullable = false)
    private Timestamp lastUpdated; // 最后更新时间
}