package org.sustech.orion.dto.responseDTO;

import lombok.Data;
import org.sustech.orion.dto.AttachmentDTO;
import org.sustech.orion.model.Assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AssignmentAttachmentResponseDTO {
    private Long assignmentId;
    private String assignmentTitle;
    private List<AttachmentDTO> attachments = new ArrayList<>();
    
    public static AssignmentAttachmentResponseDTO fromAssignment(Assignment assignment) {
        AssignmentAttachmentResponseDTO dto = new AssignmentAttachmentResponseDTO();
        dto.setAssignmentId(assignment.getId());
        dto.setAssignmentTitle(assignment.getTitle());
        
        if (assignment.getAttachments() != null) {
            dto.setAttachments(
                assignment.getAttachments().stream()
                    .map(AttachmentDTO::fromAttachment)
                    .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
} 