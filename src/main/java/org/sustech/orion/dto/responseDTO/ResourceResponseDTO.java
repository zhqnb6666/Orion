package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
// 对应Resource模型
public class ResourceResponseDTO {
    private Long id;
    private String title;
    private String type;
    private String description;
    private Timestamp uploadTime;
    private List<AttachmentResponseDTO> attachments;
}
