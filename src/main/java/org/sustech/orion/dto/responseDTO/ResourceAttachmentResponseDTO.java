package org.sustech.orion.dto.responseDTO;

import lombok.Data;
import org.sustech.orion.dto.AttachmentDTO;
import org.sustech.orion.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ResourceAttachmentResponseDTO {
    private Long resourceId;
    private String resourceName;
    private String resourceType;
    private List<AttachmentDTO> attachments = new ArrayList<>();
    
    public static ResourceAttachmentResponseDTO fromResource(Resource resource) {
        ResourceAttachmentResponseDTO dto = new ResourceAttachmentResponseDTO();
        dto.setResourceId(resource.getId());
        dto.setResourceName(resource.getName());
        dto.setResourceType(resource.getType());
        
        if (resource.getAttachments() != null) {
            dto.setAttachments(
                resource.getAttachments().stream()
                    .map(AttachmentDTO::fromAttachment)
                    .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
} 