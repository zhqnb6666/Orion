package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "submission_contents")
public class SubmissionContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // 文件/代码/文本

    @Column(columnDefinition = "TEXT")
    private String content; // 文本内容

    private String fileUrl; // 文件URL

    private String mimeType; // 文件类型

    private Long fileSize; // 文件大小

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
}