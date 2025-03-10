package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.AssignmentDTO;
import org.sustech.orion.dto.SubmissionDTO;
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
    public ResponseEntity<List<Assignment>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getActiveAssignments(courseId));
    }

    @PostMapping("/{assignmentId}/submissions")
    @Operation(summary = "创建新提交")
    public ResponseEntity<Submission> createSubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student,
            @RequestBody SubmissionDTO request) {

        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        submission.setSubmitTime(new Timestamp(System.currentTimeMillis()));
        submission.setAttempts(submissionService.getSubmissionAttempts(student.getUserId(), assignmentId) + 1);

        submission.setContents(request.getContents().stream()
                .peek(content -> content.setSubmission(submission))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(assignmentService.createSubmission(assignmentId, submission));
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

        // 使用正确的字段名称
        Integer maxAttempts = config.getMaxSubmissionAttempts();

        // 剩余计算逻辑保持不变
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
                    @ApiResponse(responseCode = "200", description = "成功获取资源列表"),
                    @ApiResponse(responseCode = "403", description = "未参加该作业"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<CourseMaterialResponseDTO> getAssignmentResources(
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


}