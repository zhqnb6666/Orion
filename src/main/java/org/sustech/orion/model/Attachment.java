package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Timestamp uploadedAt;

    // 后端可以通过 FileSizeChecker 类来检查文件大小, 但是速度较慢, 尽量少调用, 前端传输时应当附带文件大小信息
    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private AttachmentType attachmentType;

    public enum AttachmentType {
        Resource, Submission
    }
}
