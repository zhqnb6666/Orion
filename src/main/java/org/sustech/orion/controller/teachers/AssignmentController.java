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
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.util.ConvertDTO;

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
    public ResponseEntity<AssignmentResponseDTO> createAssignment(
            @PathVariable Long courseId,
            @RequestBody AssignmentDTO request) {
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setType(request.getType());
        assignment.setDueDate(request.getDueDate());
        assignment.setStatus(Assignment.Status.valueOf(request.getStatus()));
        assignment.setMaxScore(request.getMaxScore());
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignmentService.createAssignment(assignment, courseId)));
    }

    @GetMapping("/{assignmentId}/feedback")
    @Operation(summary = "获取作业反馈",
            description = "获取指定作业的所有评分反馈",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取反馈"),
                    @ApiResponse(responseCode = "403", description = "未参加该作业"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<List<GradeResponseDTO>> getAssignmentFeedback(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User student) {

        // 验证作业存在性
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证学生课程权限
        if (!courseService.isStudentInCourse(assignment.getCourse().getId(), student.getUserId())) {
            throw new ApiException("Did not participate in the course assignment", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ConvertDTO.toGradeResponseDTOList(gradeService.getFeedbackForAssignment(assignmentId, student.getUserId())));
    }

    @PutMapping("/{assignmentId}")
    @Operation(summary = "更新作业信息",
            description = "更新作业标题、描述、截止日期等基本信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新成功"),
                    @ApiResponse(responseCode = "403", description = "无修改权限"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentDTO request,
            @AuthenticationPrincipal User currentUser) {

        Assignment existing = assignmentService.getAssignmentById(assignmentId);

        // 通过课程验证教师权限
        Course course = existing.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to modify this job", HttpStatus.FORBIDDEN);
        }

        // 更新可修改字段
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setType(request.getType());
        existing.setDueDate(request.getDueDate());
        existing.setMaxScore(request.getMaxScore());
        existing.setStatus(Assignment.Status.valueOf(request.getStatus()));

        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignmentService.updateAssignment(existing)));
    }

    @DeleteMapping("/{assignmentId}")
    @Operation(summary = "删除作业",
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功"),
                    @ApiResponse(responseCode = "403", description = "无删除权限"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 通过课程验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to delete this job", HttpStatus.FORBIDDEN);
        }

        assignmentService.deleteAssignmentWithDependencies(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{assignmentId}/publish")
    @Operation(summary = "发布作业",
            description = "设置作业可见状态为公开",
            responses = {
                    @ApiResponse(responseCode = "200", description = "发布成功"),
                    @ApiResponse(responseCode = "403", description = "无操作权限"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<AssignmentResponseDTO> publishAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 通过课程验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to delete this job", HttpStatus.FORBIDDEN);
        }

        assignment.setStatus(Assignment.Status.OPEN);
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignmentService.updateAssignment(assignment)));
    }

    @PutMapping("/{assignmentId}/unpublish")
    @Operation(summary = "取消发布作业",
            description = "设置作业可见状态为不可见",
            responses = {
                    @ApiResponse(responseCode = "200", description = "取消发布成功"),
                    @ApiResponse(responseCode = "403", description = "无操作权限"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<AssignmentResponseDTO> unpublishAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 通过课程验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to delete this job", HttpStatus.FORBIDDEN);
        }

        assignment.setStatus(Assignment.Status.CLOSED);
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignmentService.updateAssignment(assignment)));
    }


    /* useless */
    @GetMapping("/course/{courseId}/active")//ok
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignmentService.getActiveAssignments(courseId)));
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