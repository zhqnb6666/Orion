package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.AssignmentDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.GradeService;

import java.sql.Timestamp;
import java.util.List;

@RestController("teachersAssignmentController")
@RequestMapping("/api/teachers/assignments")
@Tag(name = "Assignment API", description = "APIs for assignment management")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final GradeService gradeService;

    public AssignmentController(AssignmentService assignmentService, CourseService courseService, GradeService gradeService) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.gradeService = gradeService;
    }
    /* useful */
    @PostMapping("/{courseId}/assignments")//ok
    @Operation(summary = "Create assignment")
    public ResponseEntity<Assignment> createAssignment(
            @PathVariable Long courseId,
            @RequestBody AssignmentDTO request) {
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setType(request.getType());
        assignment.setDueDate(request.getDueDate());
        assignment.setIsVisible(request.getIsVisible());
        assignment.setMaxScore(request.getMaxScore());
        return ResponseEntity.ok(assignmentService.createAssignment(assignment, courseId));
    }
    @GetMapping("/{assignmentId}/feedback")
    @Operation(summary = "获取作业反馈",
            description = "获取指定作业的所有评分反馈",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取反馈"),
                    @ApiResponse(responseCode = "403", description = "未参加该作业"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<List<Grade>> getAssignmentFeedback(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        // 验证作业存在性
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(assignment.getCourse().getId(), student.getUserId())) {
            throw new ApiException("未参加该课程作业", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(gradeService.getFeedbackForAssignment(assignmentId, student.getUserId()));
    }




    /* useless */
    @GetMapping("/course/{courseId}/active")//ok
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<Assignment>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getActiveAssignments(courseId));
    }

    /*
    @PatchMapping("/{assignmentId}/due-date")
    @Operation(summary = "Extend due date")
    public ResponseEntity<Void> extendDueDate(
            @PathVariable Long assignmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Timestamp newDueDate) {
        assignmentService.extendDueDate(assignmentId, newDueDate);
        return ResponseEntity.noContent().build();
    }
    */

}