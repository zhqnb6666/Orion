package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.dto.responseDTO.SubmissionResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.User;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.util.ConvertDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("teachersSubmissionController")
@RequestMapping("/api/teachers/submissions")
@Tag(name = "Teacher Submission API", description = "APIs for assignment submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;

    public SubmissionController(SubmissionService submissionService, AssignmentService assignmentService) {
        this.submissionService = submissionService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{submissionId}")
    @Operation(summary = "获取提交详情",
            description = "获取指定ID的提交详细信息")
    public ResponseEntity<SubmissionResponseDTO> getSubmission(@PathVariable Long submissionId) {

        return ResponseEntity.ok(ConvertDTO.toSubmissionResponseDTO(submissionService.getSubmissionOrThrow(submissionId)));
    }

    @GetMapping("/assignment/{assignmentId}/latest")
    @Operation(summary = "获取作业的所有最新提交",
            description = "获取指定作业的所有学生最新提交（每个学生只返回最新的一次提交）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取提交列表"),
                    @ApiResponse(responseCode = "403", description = "无权访问该作业的提交"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<List<SubmissionResponseDTO>> getLatestSubmissionsByAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User teacher) {

        // 验证作业存在性并检查教师权限
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (!assignment.getCourse().getInstructor().getUserId().equals(teacher.getUserId())) {
            throw new ApiException("无权查看该作业的提交", HttpStatus.FORBIDDEN);
        }

        // 获取所有最新提交
        Map<Long, Submission> latestSubmissions = submissionService.getLatestSubmissionsByAssignment(assignmentId);

        // 转换为列表返回
        return ResponseEntity.ok(ConvertDTO.toSubmissionResponseDTOList(new ArrayList<>(latestSubmissions.values())));
    }

    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    @Operation(summary = "获取学生的作业提交历史",
            description = "获取特定学生对指定作业的所有提交记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取提交历史"),
                    @ApiResponse(responseCode = "403", description = "无权访问该学生的提交"),
                    @ApiResponse(responseCode = "404", description = "作业或学生不存在")
            })
    public ResponseEntity<List<SubmissionResponseDTO>> getStudentSubmissionsForAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal User teacher) {

        // 验证作业存在性并检查教师权限
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (!assignment.getCourse().getInstructor().getUserId().equals(teacher.getUserId())) {
            throw new ApiException("无权查看该作业的提交", HttpStatus.FORBIDDEN);
        }

        // 获取学生的所有提交记录
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentAndStudent(assignmentId, studentId);

        return ResponseEntity.ok(ConvertDTO.toSubmissionResponseDTOList(submissions));
    }
}
