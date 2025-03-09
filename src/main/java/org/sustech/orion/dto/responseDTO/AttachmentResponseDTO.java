package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 附件专用DTO
public class AttachmentResponseDTO {
    private Long id;
    private String name;
    private String size;
}
