package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Entity
@Data
@Table(name = "submission_contents")
public class SubmissionContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ContentType type; // 文件/代码/文本

    @Column(columnDefinition = "TEXT")
    private String content; // 文本内容

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "file_id")
    private Attachment file;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "code_id")
    private CodeSubmission codeSubmission;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Getter
    public enum ContentType {
        FILE, CODE, TEXT;
        private final String value;
        ContentType() {
            this.value = this.name().toLowerCase();
        }
    }
}