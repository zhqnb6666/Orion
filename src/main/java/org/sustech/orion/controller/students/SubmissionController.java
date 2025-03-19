package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.*;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.util.ConvertDTO;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController("studentsSubmissionController")
@RequestMapping("/api/students/submissions")
@Tag(name = "Submission API", description = "APIs for assignment submissions")
public class SubmissionController {


    private final ResourceService resourceService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;
    private final AssignmentService assignmentService;

    public SubmissionController(SubmissionService submissionService,
                                ResourceService resourceService, GradeService gradeService, AssignmentService assignmentService) {
        this.submissionService = submissionService;
        this.resourceService = resourceService;
        this.gradeService = gradeService;
        this.assignmentService = assignmentService;
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
    /*
    @PostMapping("/{submissionId}/status")
    @Operation(summary = "Update submission status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long submissionId,
            @RequestBody SubmissionDTO request) {
        submissionService.updateSubmissionStatus(submissionId, request.getNewStatus());
        return ResponseEntity.noContent().build();
    }
     */

    /*example
    * const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify({
        contents: [{
            type: "CODE",
            content: "System.out.println('Hello World');"
        }]
    })], {type: "application/json"}));

    // 添加文件
    const fileInput = document.getElementById('file-input');
    for (let i = 0; i < fileInput.files.length; i++) {
        formData.append('files', fileInput.files[i]);
    }

    fetch('/api/students/submissions/assignments/123/submissions', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        body: formData
    });
    * */
    /*
    @PostMapping(value = "/assignments/{assignmentId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建新提交（支持文件上传）")
    public ResponseEntity<Submission> createSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @ModelAttribute SubmissionDTO request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        // 处理文件上传
        if (files != null && !files.isEmpty()) {
            List<SubmissionDTO.SubmissionContentDTO> fileContents = files.stream().map(file -> {
                String fileUrl = resourceService.uploadFile(file);
                SubmissionDTO.SubmissionContentDTO content = new SubmissionDTO.SubmissionContentDTO();
                content.setType(SubmissionContent.ContentType.FILE);
                content.setFileUrl(fileUrl);
                return content;
            }).collect(Collectors.toList());

            request.getContents().addAll(fileContents);
        }

        // 构建Submission实体
        Submission submission = new Submission();
        submission.setContents(request.getContents().stream()
                .map(dto -> {
                    SubmissionContent content = new SubmissionContent();
                    content.setType(dto.getType());
                    content.setContent(dto.getContent());
                    content.setFileUrl(dto.getFileUrl());
                    content.setSubmission(submission);
                    return content;
                })
                .collect(Collectors.toList()));

        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        submission.setStudent(student);

        return ResponseEntity.ok(submissionService.createSubmission(assignmentId, submission));
    }
    */


    /*
    * 调用示例：
    * const formData = new FormData();
formData.append('textResponse', '这是我的文本回答');
formData.append('uploadFile', fileInput1.files[0]);
formData.append('uploadFile', fileInput2.files[0]);

fetch(`/api/students/submissions/assignments/${assignmentId}/submissions`, {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer ' + token
    },
    body: formData
});
    * */
    @PostMapping(value = "/assignments/{assignmentId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建新提交（支持文件上传）")
    public ResponseEntity<Submission> createSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @RequestParam(value = "textResponse", required = false) String textResponse,
            @RequestPart(value = "uploadFile", required = false) List<MultipartFile> files) {


        // 获取提交配置
        SubmissionConfig config = submissionService.getSubmissionConfigByAssignmentId(assignmentId);

        // 处理文本内容
        Submission submission = new Submission();
        if (StringUtils.hasText(textResponse)) {
            SubmissionContent textContent = new SubmissionContent();
            textContent.setType(SubmissionContent.ContentType.TEXT);
            textContent.setContent(textResponse);
            textContent.setSubmission(submission);
            submission.getContents().add(textContent);
        }

        // 处理文件上传
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                // 验证文件大小
                if (file.getSize() > config.getMaxFileSize()) {
                    throw new ApiException("The file size exceeds the limit: " + config.getMaxFileSize() + " bytes", HttpStatus.BAD_REQUEST);
                }

                // 验证文件类型
                String fileName = file.getOriginalFilename();
                String fileType = fileName != null ?
                        fileName.substring(fileName.lastIndexOf(".") + 1) : "";
                if (!Arrays.asList(config.getAllowedFileTypes().split(",")).contains(fileType)) {
                    throw new ApiException("Disallowed file type: " + fileType, HttpStatus.BAD_REQUEST);
                }

                // 上传文件
                String fileUrl = resourceService.uploadFile(file);

                SubmissionContent fileContent = new SubmissionContent();
                fileContent.setType(SubmissionContent.ContentType.FILE);
                fileContent.setFileUrl(fileUrl);
                fileContent.setFileSize(file.getSize());
                fileContent.setMimeType(file.getContentType());
                fileContent.setSubmission(submission);
                submission.getContents().add(fileContent);
            }
        }

        // 设置基础信息
        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        submission.setStudent(student);
        submission.setSubmitTime(new Timestamp(System.currentTimeMillis()));

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
            throw new ApiException("The commit is not authorized to operate", HttpStatus.FORBIDDEN);
        }

        // 状态校验

        if (!Submission.SubmissionStatus.DRAFT.equals(submission.getStatus())) {
            throw new ApiException("Only draft status can be modified", HttpStatus.BAD_REQUEST);
        }

        // 上传文件并创建内容记录
        String fileUrl = resourceService.uploadFile(file);
        SubmissionContent content = new SubmissionContent();
        content.setType(SubmissionContent.ContentType.FILE);
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
