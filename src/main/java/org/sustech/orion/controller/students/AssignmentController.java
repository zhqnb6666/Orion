package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.AssignmentAttachmentResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.*;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;
import java.io.IOException;

@RestController("studentsAssignmentController")
@RequestMapping("/api/students/assignments")
@Tag(name = "Student Assignment API", description = "APIs for assignment management")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CourseService courseService;
    private final ResourceService resourceService;
    private final AttachmentService attachmentService;

    public AssignmentController(AssignmentService assignmentService, SubmissionService submissionService, CourseService courseService, ResourceService resourceService, AttachmentService attachmentService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.courseService = courseService;
        this.resourceService = resourceService;
        this.attachmentService = attachmentService;
    }
    /* useful */


    /* useless */
    @GetMapping("/course/{courseId}/active")
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignmentService.getActiveAssignments(courseId)));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程所有作业", 
            description = "获取指定课程的所有作业列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取作业列表"),
                    @ApiResponse(responseCode = "403", description = "未参加该课程"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments(@PathVariable Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignments));
    }


    

    @GetMapping("/{assignmentId}/submissions")
    @Operation(summary = "获取提交历史")
    public ResponseEntity<List<Submission>> getSubmissions(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        return ResponseEntity.ok(
                assignmentService.getSubmissionsByAssignmentAndStudent(assignmentId, student.getUserId())
        );
    }

    @GetMapping("/{assignmentId}/submissions/remaining")
    @Operation(summary = "Get remaining submission attempts")
    public ResponseEntity<Integer> getRemainingAttempts(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        // 获取提交配置
        SubmissionConfig config = assignmentService.getSubmissionConfigByAssignmentId(assignmentId);


        Integer maxAttempts = config.getMaxSubmissionAttempts();


        Integer usedAttempts = submissionService.getSubmissionAttempts(
                student.getUserId(),
                assignmentId
        );

        int remaining = maxAttempts - usedAttempts;
        return ResponseEntity.ok(Math.max(remaining, 0));
    }
    @GetMapping("/{assignmentId}/details")
    @Operation(summary = "获取作业详细信息",
            description = "获取与指定作业相关的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取作业详细信息"),
                    @ApiResponse(responseCode = "403", description = "未参加该课程作业"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<AssignmentResponseDTO> getAssignmentDetails(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(
                assignment.getCourse().getId(),
                student.getUserId())) {
            throw new ApiException("未参加该课程作业", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignment));
    }

    /**
     * 获取作业的所有附件
     * @param assignmentId 作业ID
     * @param student 当前学生
     * @return 附件列表
     */
    @GetMapping("/{assignmentId}/attachments")
    @Operation(summary = "获取作业附件列表",
            description = "获取指定作业的所有附件文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "403", description = "未参加该课程"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<AssignmentAttachmentResponseDTO> getAssignmentAttachments(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(assignment.getCourse().getId(), student.getUserId())) {
            throw new ApiException("未参加该课程", HttpStatus.FORBIDDEN);
        }

        // 返回作业及附件信息
        return ResponseEntity.ok(AssignmentAttachmentResponseDTO.fromAssignment(assignment));
    }

    /**
     * 下载作业附件
     * @param assignmentId 作业ID
     * @param attachmentId 附件ID
     * @param student 当前学生
     * @return 附件内容
     * @throws IOException 文件读取错误
     */
    @GetMapping("/{assignmentId}/attachments/{attachmentId}/download")
    @Operation(summary = "下载作业附件",
            description = "下载指定作业的附件文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "下载成功"),
                    @ApiResponse(responseCode = "403", description = "未参加该课程"),
                    @ApiResponse(responseCode = "404", description = "作业或附件不存在")
            })
    public ResponseEntity<Resource> downloadAssignmentAttachment(
            @PathVariable Long assignmentId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal User student) throws IOException {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(assignment.getCourse().getId(), student.getUserId())) {
            throw new ApiException("未参加该课程", HttpStatus.FORBIDDEN);
        }

        // 获取附件信息
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        
        // 下载附件内容
        byte[] data = attachmentService.downloadAttachment(attachmentId);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .contentLength(attachment.getSize())
                .body(resource);
    }

}