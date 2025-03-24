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
import org.sustech.orion.dto.AssignmentDTO;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.CourseMaterialResponseDTO;
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

@RestController("studentsAssignmentController")
@RequestMapping("/api/students/assignments")
@Tag(name = "Assignment API", description = "APIs for assignment management")
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
                        
                        // 创建提交内容
                        SubmissionContent fileContent = new SubmissionContent();
                        fileContent.setType(SubmissionContent.ContentType.FILE);
                        
                        // 创建JSON存储附件信息
                        String attachmentInfo = String.format(
                            "{\"attachmentId\":%d,\"url\":\"%s\",\"size\":%d,\"mimeType\":\"%s\",\"name\":\"%s\"}",
                            attachment.getId(),
                            attachment.getUrl(),
                            attachment.getSize(),
                            attachment.getMimeType(),
                            attachment.getName()
                        );
                        
                        fileContent.setContent(attachmentInfo);
                        fileContent.setSubmission(submission);
                        submission.getContents().add(fileContent);
                    } catch (Exception e) {
                        throw new ApiException("文件上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
        
        // 保存提交
        submissionService.saveSubmission(submission);
        
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
    @GetMapping("/{assignmentId}/resources")
    @Operation(summary = "获取作业资源",
            description = "获取与指定作业相关的学习资源",
            responses = {
                    @ApiResponse(responseCode = "200", description = "The resource list is successfully obtained. Procedure"),
                    @ApiResponse(responseCode = "403", description = "Did not participate in the course assignment"),
                    @ApiResponse(responseCode = "404", description = "assignment does not exist")
            })
    public ResponseEntity<CourseMaterialResponseDTO> getAssignmentResources(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(
                assignment.getCourse().getId(),
                student.getUserId())) {
            throw new ApiException("Did not participate in the course assignment", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ConvertDTO.AssignmentToCourseMaterialResponseDTO(assignment));
    }


}