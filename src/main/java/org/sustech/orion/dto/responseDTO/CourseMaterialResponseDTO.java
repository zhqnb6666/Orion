package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// 对应Resource模型
public class CourseMaterialResponseDTO {
    private String id;
    private String title;
    private String type;
    private String description;
    private List<AttachmentResponseDTO> attachments;
}
