package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.SubmissionContent;
import org.sustech.orion.model.User;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;
import java.util.stream.Collectors;

@RestController("studentsSubmissionController")
@RequestMapping("/api/students/submissions")
@Tag(name = "Submission API", description = "APIs for assignment submissions")
public class SubmissionController {


    private final ResourceService resourceService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;

    public SubmissionController(SubmissionService submissionService,
                                ResourceService resourceService,GradeService gradeService) {
        this.submissionService = submissionService;
        this.resourceService = resourceService;
        this.gradeService = gradeService;
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

    @PostMapping("/{submissionId}/status")
    @Operation(summary = "Update submission status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long submissionId,
            @RequestBody SubmissionDTO request) {
        submissionService.updateSubmissionStatus(submissionId, request.getNewStatus());
        return ResponseEntity.noContent().build();
    }

    // 创建新提交
    @PostMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Create new submission")
    public ResponseEntity<Submission> createSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @RequestBody SubmissionDTO request) {
        Submission submission = new Submission();
        /*
        {
            "contents": [
                {
                    "type": "CODE",
                    "content": "System.out.println(\"Hello World\");",
                    "fileUrl": null
                },
                {
                    "type": "FILE",
                    "fileUrl": "/uploads/file1.pdf",
                    "mimeType": "application/pdf"
                }
            ]
        }
        */
        submission.setContents(request.getContents().stream()
                .peek(content -> content.setSubmission(submission))
                .collect(Collectors.toList()));

        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        submission.setStudent(student);
        return ResponseEntity.ok(submissionService.createSubmission(assignmentId, submission));
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

    @PostMapping("/{submissionId}/files")
    @Operation(summary = "Upload file to submission",
            description = "Add file to DRAFT submission")
    public ResponseEntity<SubmissionContent> uploadFile(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student,
            @RequestParam("file") MultipartFile file) {

        Submission submission = submissionService.getSubmissionOrThrow(submissionId);

        // 权限校验
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("无权操作该提交", HttpStatus.FORBIDDEN);
        }

        // 状态校验
        if (!"DRAFT".equals(submission.getStatus())) {
            throw new ApiException("仅草稿状态可修改", HttpStatus.BAD_REQUEST);
        }

        // 上传文件并创建内容记录
        String fileUrl = resourceService.uploadFile(file);
        SubmissionContent content = new SubmissionContent();
        content.setType("FILE");
        content.setFileUrl(fileUrl);
        content.setMimeType(file.getContentType());
        content.setFileSize(file.getSize());
        content.setSubmission(submission);

        submission.getContents().add(content);
        submissionService.saveSubmission(submission);

        return ResponseEntity.ok(content);
    }

    // src/main/java/org/sustech/orion/controller/students/SubmissionController.java
    @GetMapping("/{submissionId}/grade")
    @Operation(summary = "获取提交评分详情",
            description = "获取指定提交的评分和评语",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取评分信息"),
                    @ApiResponse(responseCode = "403", description = "无权访问该提交"),
                    @ApiResponse(responseCode = "404", description = "提交或评分不存在")
            })
    public ResponseEntity<GradeResponseDTO> getSubmissionGrade(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student) {

        Submission submission = submissionService.getSubmissionOrThrow(submissionId);

        // 权限校验
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("无权查看该提交评分", HttpStatus.FORBIDDEN);
        }

        if (submission.getGrade() == null) {
            throw new ApiException("该提交尚未评分", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(ConvertDTO.toGradeResponseDTO( submission.getGrade()));
    }
    @PostMapping("/{submissionId}/appeal")
    @Operation(summary = "提交成绩申诉",
            description = "对已评分的提交提出申诉",
            responses = {
                    @ApiResponse(responseCode = "201", description = "申诉已提交"),
                    @ApiResponse(responseCode = "403", description = "无权操作"),
                    @ApiResponse(responseCode = "409", description = "申诉已存在")
            })
    public ResponseEntity<Void> submitAppeal(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User student,
            @RequestBody String appealReason) {

        Submission submission = submissionService.getSubmissionOrThrow(submissionId);

        // 权限校验
        if (!submission.getStudent().getUserId().equals(student.getUserId())) {
            throw new ApiException("无权操作该提交", HttpStatus.FORBIDDEN);
        }

        // 检查评分状态
        if (submission.getGrade() == null || !submission.getGrade().getIsFinalized()) {
            throw new ApiException("成绩未最终确认", HttpStatus.BAD_REQUEST);
        }

        gradeService.submitGradeAppeal(submission.getGrade().getId(), appealReason);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* useless */
}
