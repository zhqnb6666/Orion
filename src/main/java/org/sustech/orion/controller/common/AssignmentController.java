package org.sustech.orion.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.AssignmentAttachmentResponseDTO;
import org.sustech.orion.model.*;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.util.ConvertDTO;
import java.util.List;

@RestController("CommonAssignmentController")
@RequestMapping("/api/common/assignments")
@Tag(name = "Common Assignment API", description = "APIs for common assignment management")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService, SubmissionService submissionService, CourseService courseService, ResourceService resourceService, AttachmentService attachmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/course/{courseId}/active")
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignmentService.getActiveAssignments(courseId)));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程所有作业",
            description = "获取指定课程的所有作业列表"
            )
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments(@PathVariable Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignments));
    }

    @GetMapping("/{assignmentId}/details")
    @Operation(summary = "获取作业详细信息",
            description = "获取与指定作业相关的详细信息")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentDetails(
            @PathVariable Long assignmentId) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignment));
    }

    @GetMapping("/{assignmentId}/attachments")
    @Operation(summary = "获取作业附件列表",
            description = "获取指定作业的所有附件文件")
    public ResponseEntity<AssignmentAttachmentResponseDTO> getAssignmentAttachments(
            @PathVariable Long assignmentId) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(AssignmentAttachmentResponseDTO.fromAssignment(assignment));
    }
}