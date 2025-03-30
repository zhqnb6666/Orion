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

    @PostMapping(value = "/{assignmentId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建新提交（支持文件上传）")
    public ResponseEntity<Submission> createSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @ModelAttribute SubmissionDTO request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        // 获取作业
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

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

    // 获取作业提交历史（需补充到AssignmentController.java）
    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Get submission history")
    public ResponseEntity<List<Submission>> getSubmissionHistory(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignmentAndStudent(assignmentId, student.getUserId()));
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
