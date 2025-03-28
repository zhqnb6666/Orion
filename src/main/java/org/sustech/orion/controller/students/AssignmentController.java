package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.sustech.orion.dto.AssignmentDTO;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.CourseMaterialResponseDTO;
import org.sustech.orion.dto.responseDTO.AssignmentAttachmentResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.*;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.util.ConvertDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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


    @PostMapping(value = "/{assignmentId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建新提交（支持文件上传）")
    public ResponseEntity<Submission> createSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @ModelAttribute SubmissionDTO request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        // 获取作业
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生是否参加了该课程
        if (!courseService.isStudentInCourse(assignment.getCourse().getId(), student.getUserId())) {
            throw new ApiException("没有参加该课程", HttpStatus.FORBIDDEN);
        }
        
        // 检查截止日期
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (now.after(assignment.getDueDate())) {
            throw new ApiException("作业已截止提交", HttpStatus.BAD_REQUEST);
        }

        // 获取提交配置
        SubmissionConfig config = assignmentService.getSubmissionConfigByAssignmentId(assignmentId);
        
        // 检查剩余提交次数
        Integer maxAttempts = config.getMaxSubmissionAttempts();
        Integer usedAttempts = submissionService.getSubmissionAttempts(student.getUserId(), assignmentId);
        
        if (usedAttempts >= maxAttempts) {
            throw new ApiException("已达到最大提交次数", HttpStatus.BAD_REQUEST);
        }

        // 创建提交
        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setAssignment(assignment);
        submission.setSubmitTime(now);
        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        submission.setContents(new ArrayList<>());
        
        // 添加文本内容（如果有）
        if (StringUtils.hasText(request.getTextResponse())) {
            SubmissionContent textContent = new SubmissionContent();
            textContent.setType(SubmissionContent.ContentType.TEXT);
            textContent.setContent(request.getTextResponse());
            textContent.setSubmission(submission);
            submission.getContents().add(textContent);
        }
        
        // 处理文件附件（如果有）
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // 上传附件
                        Attachment attachment = attachmentService.uploadAttachment(file, Attachment.AttachmentType.Submission);
                        
                        // 创建提交内容，将附件信息存储在file字段中
                        SubmissionContent fileContent = new SubmissionContent();
                        fileContent.setType(SubmissionContent.ContentType.FILE);
                        fileContent.setFile(attachment); // 直接设置file字段
                        fileContent.setSubmission(submission);
                        
                        // 更新提交
                        submission.getContents().add(fileContent);
                        submissionService.saveSubmission(submission);
                    } catch (Exception e) {
                        throw new ApiException("文件上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
        
        return ResponseEntity.ok(submission);
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
    public ResponseEntity<CourseMaterialResponseDTO> getAssignmentDetails(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(
                assignment.getCourse().getId(),
                student.getUserId())) {
            throw new ApiException("未参加该课程作业", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ConvertDTO.AssignmentToCourseMaterialResponseDTO(assignment));
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