package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "ai_gradings")
public class AIGrading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private Double aiScore; // AI给出的分数

    @Column(nullable = false)
    private Double confidence; // 置信度

    @Column(columnDefinition = "TEXT")
    private String feedbackSuggestions; // 反馈建议
}