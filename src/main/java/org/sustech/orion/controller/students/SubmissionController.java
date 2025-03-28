package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.dto.CodeSubmissionDTO;
import org.sustech.orion.dto.CodeSubmissionResult;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.*;
import org.sustech.orion.service.*;
import org.sustech.orion.util.ConvertDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController("studentsSubmissionController")
@RequestMapping("/api/students/submissions")
@Tag(name = "Submission API", description = "APIs for assignment submissions")
public class SubmissionController {


    private final ResourceService resourceService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;
    private final AssignmentService assignmentService;
    private final CodeService codeService;
    private final AttachmentService attachmentService;

    public SubmissionController(SubmissionService submissionService,
                                ResourceService resourceService, GradeService gradeService, AssignmentService assignmentService, CodeService codeService, AttachmentService attachmentService) {
        this.submissionService = submissionService;
        this.resourceService = resourceService;
        this.gradeService = gradeService;
        this.assignmentService = assignmentService;
        this.codeService = codeService;
        this.attachmentService = attachmentService;
    }

    /* useful */
    @GetMapping("/{submissionId}")
    @Operation(summary = "Get submission details")
    public ResponseEntity<Submission> getSubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getSubmissionOrThrow(submissionId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List submissions by status")
    public ResponseEntity<List<Submission>> getSubmissionsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(submissionService.getSubmissionsByStatus(status));
    }

    //似乎学生端不需要?
    @PostMapping("/{submissionId}/status")
    @Operation(summary = "Update submission status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long submissionId,
            @RequestParam String newStatus) {
        submissionService.updateSubmissionStatus(submissionId, newStatus);
        return ResponseEntity.noContent().build();
    }

    //提交代码作业并且异步运行
    @PostMapping("/assignments/{assignmentId}/submissions/code")
    @Operation(summary = "Create a new code submission")
    public ResponseEntity<Submission> createCodeSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @RequestBody CodeSubmissionDTO codeSubmissionDTO) {
        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setAssignment(assignmentService.getAssignmentById(assignmentId));
        submission.setSubmitTime(new Timestamp(System.currentTimeMillis()));
        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        SubmissionContent content = new SubmissionContent();
        content.setType(SubmissionContent.ContentType.CODE);
        CodeSubmission codeSubmission = new CodeSubmission();
        codeSubmission.setScript(codeSubmissionDTO.getScript());
        codeSubmission.setLanguage(codeSubmissionDTO.getLanguage());
        codeSubmission.setVersionIndex(codeSubmissionDTO.getVersionIndex());
        content.setCodeSubmission(codeSubmission);
        content.setSubmission(submission);
        submission.setContents(List.of(content));
        Submission savedSubmission = submissionService.createSubmission(assignmentId, submission);
        CompletableFuture.runAsync(() -> {
            codeService.executeAndEvaluateSubmission(savedSubmission.getId());
        });
        return ResponseEntity.ok(savedSubmission);
    }

    //获得代码作业的运行结果
    @GetMapping("/assignments/submissions/code/{submissionId}")
    @Operation(summary = "Get code submission result")
    public ResponseEntity<CodeSubmissionResult> getCodeSubmissionResult(
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getCodeSubmissionResult(submissionId));
    }
    
    /**
     * 创建提交并准备附件上传
     * @param assignmentId 作业ID
     * @param student 当前学生
     * @param fileCount 文件数量
     * @param textResponse 文本回答内容
     * @return 创建的提交及附件占位符列表
     */
    @PostMapping("/assignments/{assignmentId}/prepare")
    @Operation(summary = "创建提交并准备附件上传",
              description = "创建一个新的提交，并生成指定数量的附件占位符，用于后续异步上传文件",
              responses = {
                  @ApiResponse(responseCode = "200", description = "创建成功"),
                  @ApiResponse(responseCode = "400", description = "参数错误"),
                  @ApiResponse(responseCode = "404", description = "作业不存在")
              })
    public ResponseEntity<Map<String, Object>> prepareSubmissionWithAttachments(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @RequestParam(value = "fileCount", defaultValue = "0") int fileCount,
            @RequestParam(value = "textResponse", required = false) String textResponse) {
        
        // 创建提交
        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setAssignment(assignmentService.getAssignmentById(assignmentId));
        submission.setSubmitTime(new Timestamp(System.currentTimeMillis()));
        submission.setStatus(Submission.SubmissionStatus.DRAFT); // 设置为草稿状态
        
        // 添加文本内容（如果有）
        if (StringUtils.hasText(textResponse)) {
            SubmissionContent textContent = new SubmissionContent();
            textContent.setType(SubmissionContent.ContentType.TEXT);
            textContent.setContent(textResponse);
            textContent.setSubmission(submission);
            submission.getContents().add(textContent);
        }
        
        // 保存提交获取ID
        submissionService.saveSubmission(submission);
        
        // 创建附件占位符列表
        List<Map<String, Object>> attachmentPlaceholders = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            // 创建占位符附件（暂不保存到数据库）
            Map<String, Object> placeholder = new HashMap<>();
            placeholder.put("placeholderId", i);
            placeholder.put("status", "pending");
            attachmentPlaceholders.add(placeholder);
        }
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("submissionId", submission.getId());
        response.put("status", submission.getStatus());
        response.put("attachmentPlaceholders", attachmentPlaceholders);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 异步上传提交的附件
     * @param submissionId 提交ID
     * @param placeholderId 附件占位符ID
     * @param file 文件
     * @param student 当前学生
     * @return 上传的附件信息
     */
    @PostMapping("/{submissionId}/attachments/{placeholderId}")
    @Operation(summary = "异步上传提交附件",
              description = "为已创建的提交异步上传附件文件",
              responses = {
                  @ApiResponse(responseCode = "200", description = "上传成功"),
                  @ApiResponse(responseCode = "400", description = "参数错误"),
                  @ApiResponse(responseCode = "403", description = "无权限操作"),
                  @ApiResponse(responseCode = "404", description = "提交不存在")
              })
    public ResponseEntity<Map<String, Object>> uploadSubmissionAttachment(
            @PathVariable Long submissionId,
            @PathVariable Integer placeholderId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User student) {
        
        // 获取提交
        Submission submission = submissionService.getSubmissionOrThrow(submissionId);
        
        // 权限验证
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("无权限操作此提交", HttpStatus.FORBIDDEN);
        }
        
        // 状态验证
        if (!Submission.SubmissionStatus.DRAFT.equals(submission.getStatus())) {
            throw new ApiException("只能修改草稿状态的提交", HttpStatus.BAD_REQUEST);
        }
        
        // 验证文件
        if (file.isEmpty()) {
            throw new ApiException("文件为空", HttpStatus.BAD_REQUEST);
        }
        
        try {
            // 上传附件
            Attachment attachment = attachmentService.uploadAttachment(file, Attachment.AttachmentType.Submission);
            
            // 创建提交内容，将附件设置到file字段
            SubmissionContent fileContent = new SubmissionContent();
            fileContent.setType(SubmissionContent.ContentType.FILE);
            fileContent.setFile(attachment); // 直接设置file字段
            fileContent.setSubmission(submission);
            
            // 更新提交
            submission.getContents().add(fileContent);
            submissionService.saveSubmission(submission);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("placeholderId", placeholderId);
            response.put("attachmentId", attachment.getId());
            response.put("name", attachment.getName());
            response.put("status", "completed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ApiException("文件上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 完成提交过程
     * @param submissionId 提交ID
     * @param student 当前学生
     * @return 提交信息
     */
    @PostMapping("/{submissionId}/complete")
    @Operation(summary = "完成提交过程",
              description = "将草稿状态的提交转为已提交状态",
              responses = {
                  @ApiResponse(responseCode = "200", description = "提交成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作"),
                  @ApiResponse(responseCode = "404", description = "提交不存在")
              })
    public ResponseEntity<Submission> completeSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student) {
        
        // 获取提交
        Submission submission = submissionService.getSubmissionOrThrow(submissionId);
        
        // 权限验证
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("无权限操作此提交", HttpStatus.FORBIDDEN);
        }
        
        // 状态验证
        if (!Submission.SubmissionStatus.DRAFT.equals(submission.getStatus())) {
            throw new ApiException("只能提交草稿状态的提交", HttpStatus.BAD_REQUEST);
        }
        
        // 更新状态为已接受
        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        submission.setSubmitTime(new Timestamp(System.currentTimeMillis()));
        
        // 保存并返回
        submissionService.saveSubmission(submission);
        return ResponseEntity.ok(submission);
    }

    // 获取作业提交历史（需补充到AssignmentController.java）
    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Get submission history")
    public ResponseEntity<List<Submission>> getSubmissionHistory(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignmentAndStudent(assignmentId, student.getUserId()));
    }

    @DeleteMapping("/{submissionId}")
    @Operation(summary = "Delete submission",
            description = "Students can only delete their own submissions in DRAFT status")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student) {
        submissionService.deleteStudentSubmission(submissionId, student.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{submissionId}/status")
    @Operation(summary = "Check submission status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status retrieved"),
                    @ApiResponse(responseCode = "403", description = "Unauthorized access")
            })
    public ResponseEntity<String> checkSubmissionStatus(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(
                submissionService.getSubmissionStatus(submissionId, student.getUserId())
        );
    }


    // src/main/java/org/sustech/orion/controller/students/SubmissionController.java
    @GetMapping("/{submissionId}/grade")
    @Operation(summary = "获取提交评分详情",
            description = "获取指定提交的评分和评语",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scoring information is obtained successfully"),
                    @ApiResponse(responseCode = "403", description = "Do not have access to the submission"),
                    @ApiResponse(responseCode = "404", description = "Submission or rating does not exist")
            })
    public ResponseEntity<GradeResponseDTO> getSubmissionGrade(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student) {

        Submission submission = submissionService.getSubmissionOrThrow(submissionId);

        // 权限校验
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("You have no right to view the submission score", HttpStatus.FORBIDDEN);
        }

        if (submission.getGrade() == null) {
            throw new ApiException("The submission has not yet been scored", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(ConvertDTO.toGradeResponseDTO(submission.getGrade()));
    }

    @PostMapping("/{submissionId}/appeal")
    @Operation(summary = "提交成绩申诉",
            description = "对已评分的提交提出申诉",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Complaint filed"),
                    @ApiResponse(responseCode = "403", description = "No right to operate"),
                    @ApiResponse(responseCode = "409", description = "Complaint exists")
            })
    public ResponseEntity<Void> submitAppeal(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student,
            @RequestBody String appealReason) {

        Submission submission = submissionService.getSubmissionOrThrow(submissionId);

        // 权限校验
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("The commit is not authorized to operate", HttpStatus.FORBIDDEN);
        }

        // 检查评分状态
        if (submission.getGrade() == null || !submission.getGrade().getIsFinalized()) {
            throw new ApiException("Grades have not been confirmed", HttpStatus.BAD_REQUEST);
        }

        gradeService.submitGradeAppeal(submission.getGrade().getId(), appealReason);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* useless */
}
