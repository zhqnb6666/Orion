package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import org.sustech.orion.util.ConvertDTO;

import java.sql.Timestamp;
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

    public AssignmentController(AssignmentService assignmentService, SubmissionService submissionService, CourseService courseService, ResourceService resourceService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.courseService = courseService;
        this.resourceService = resourceService;
    }
    /* useful */


    /* useless */
    @GetMapping("/course/{courseId}/active")
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignmentService.getActiveAssignments(courseId)));
    }
    /*使用示例
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

    @PostMapping(value = "/{assignmentId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            }).toList();

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