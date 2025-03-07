package org.sustech.orion.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "submission_configs")
public class SubmissionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long maxFileSize; // 最大文件大小

    @Column(nullable = false)
    private String allowedFileTypes; // 允许的文件类型列表

    @Column(nullable = false)
    private Integer maxSubmissionAttempts; // 最大提交次数

    @OneToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
}