package org.sustech.orion.dto;

import lombok.Data;
import org.sustech.orion.model.Attachment;

import java.sql.Timestamp;

@Data
public class AttachmentDTO {
    private Long id;
    private String name;
    private String mimeType;
    private Long size;
    private String url;
    private Timestamp uploadedAt;
    private String type;
    
    public static AttachmentDTO fromAttachment(Attachment attachment) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setName(attachment.getName());
        dto.setMimeType(attachment.getMimeType());
        dto.setSize(attachment.getSize());
        dto.setUrl(attachment.getUrl());
        dto.setUploadedAt(attachment.getUploadedAt());
        dto.setType(attachment.getAttachmentType().name());
        return dto;
    }
} 