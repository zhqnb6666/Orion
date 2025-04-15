package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.sustech.orion.dto.AssignmentDTO;
import org.sustech.orion.dto.AttachmentDTO;
import org.sustech.orion.dto.TestcaseDTO;
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.dto.responseDTO.AssignmentAttachmentResponseDTO;
import org.sustech.orion.dto.responseDTO.TestCaseResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.TestCase;
import org.sustech.orion.model.User;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.util.ConvertDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;

@RestController("teachersAssignmentController")
@RequestMapping("/api/teachers/assignments")
@Tag(name = "Teacher Assignment API", description = "APIs for assignment management by teacher")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final GradeService gradeService;
    private final AttachmentService attachmentService;

    public AssignmentController(AssignmentService assignmentService, CourseService courseService, GradeService gradeService, AttachmentService attachmentService) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.gradeService = gradeService;
        this.attachmentService = attachmentService;
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
        assignment.setOpenDate(request.getOpenDate());
        assignment.setInstructions(request.getInstructions());
        assignment.setMaxScore(request.getMaxScore());
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTO(assignmentService.createAssignment(assignment, courseId)));
    }

    @PutMapping("/{assignmentId}/testcases/{testcaseId}")
    @Operation(summary = "Update test case",
            description = "Update the test case for a specific assignment",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Update successful"),
                    @ApiResponse(responseCode = "403", description = "No permission to modify"),
                    @ApiResponse(responseCode = "404", description = "Assignment or test case not found")
            })
    public ResponseEntity<TestCaseResponseDTO> updateTestcase(
            @PathVariable Long assignmentId,
            @PathVariable Long testcaseId,
            @RequestBody TestcaseDTO request,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // Validate instructor permission
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to modify this testcase", HttpStatus.FORBIDDEN);
        }

        TestCase testcase = assignmentService.getTestCaseById(testcaseId);
        if (testcase == null || !testcase.getAssignment().getId().equals(assignmentId)) {
            throw new ApiException("Testcase not found", HttpStatus.NOT_FOUND);
        }

        // Update test case fields
        testcase.setInput(request.getInput());
        testcase.setExpectedOutput(request.getExpectedOutput());
        testcase.setWeight(request.getWeight());
        testcase = assignmentService.updateTestcase(testcase);
        return ResponseEntity.ok(new TestCaseResponseDTO(testcase));
    }

    @PostMapping("/{assignmentId}/testcases")
    @Operation(summary = "Upload test case",
            description = "Upload a new test case for a specific assignment",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Upload successful"),
                    @ApiResponse(responseCode = "403", description = "No permission to upload"),
                    @ApiResponse(responseCode = "404", description = "Assignment not found")
            })
    public ResponseEntity<TestCaseResponseDTO> uploadTestcase(
            @PathVariable Long assignmentId,
            @RequestBody TestcaseDTO request,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // Validate instructor permission
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to upload this testcase", HttpStatus.FORBIDDEN);
        }

        // Create and save new test case
        TestCase testcase = new TestCase();
        testcase.setInput(request.getInput());
        testcase.setExpectedOutput(request.getExpectedOutput());
        testcase.setWeight(request.getWeight());
        testcase.setAssignment(assignment);

        testcase = assignmentService.addTestcase(testcase);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TestCaseResponseDTO(testcase));
    }

    @DeleteMapping("/{assignmentId}/testcases/{testcaseId}")
    @Operation(summary = "Delete test case",
            description = "Delete a test case for a specific assignment",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Delete successful"),
                    @ApiResponse(responseCode = "403", description = "No permission to delete"),
                    @ApiResponse(responseCode = "404", description = "Assignment or test case not found")
            })
    public ResponseEntity<Void> deleteTestcase(
            @PathVariable Long assignmentId,
            @PathVariable Long testcaseId,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // Validate instructor permission
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to delete this testcase", HttpStatus.FORBIDDEN);
        }

        TestCase testcase = assignmentService.getTestCaseById(testcaseId);
        if (testcase == null || !testcase.getAssignment().getId().equals(assignmentId)) {
            throw new ApiException("Testcase not found", HttpStatus.NOT_FOUND);
        }

        assignmentService.deleteTestcase(testcaseId);
        return ResponseEntity.noContent().build();
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
        existing.setOpenDate(request.getOpenDate());
        existing.setMaxScore(request.getMaxScore());

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

    /**
     * 为作业上传附件
     * @param assignmentId 作业ID
     * @param files 文件列表
     * @param currentUser 当前用户
     * @return 附件信息列表
     */
    @PostMapping(value = "/{assignmentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传作业附件",
            description = "向指定作业添加多个附件文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上传成功"),
                    @ApiResponse(responseCode = "403", description = "无权限操作"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<List<AttachmentDTO>> uploadAssignmentAttachment(
            @PathVariable Long assignmentId,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该作业", HttpStatus.FORBIDDEN);
        }

        List<AttachmentDTO> attachmentDTOs = new ArrayList<>();
        
        // 上传多个附件
        for (MultipartFile file : files) {
            // 上传附件
            Attachment attachment = attachmentService.uploadAttachment(file, Attachment.AttachmentType.Resource);

            // 添加附件到作业
            if (assignment.getAttachments() == null) {
                assignment.setAttachments(new ArrayList<>());
            }
            assignment.getAttachments().add(attachment);
            attachmentDTOs.add(AttachmentDTO.fromAttachment(attachment));
        }
        
        assignmentService.updateAssignment(assignment);

        // 返回附件信息列表
        return ResponseEntity.ok(attachmentDTOs);
    }

    /**
     * 获取作业的所有附件
     * @param assignmentId 作业ID
     * @param currentUser 当前用户
     * @return 附件列表
     */
    @GetMapping("/{assignmentId}/attachments")
    @Operation(summary = "获取作业附件列表",
            description = "获取指定作业的所有附件文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "403", description = "无权限操作"),
                    @ApiResponse(responseCode = "404", description = "作业不存在")
            })
    public ResponseEntity<AssignmentAttachmentResponseDTO> getAssignmentAttachments(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限查看该作业", HttpStatus.FORBIDDEN);
        }

        // 返回作业及附件信息
        return ResponseEntity.ok(AssignmentAttachmentResponseDTO.fromAssignment(assignment));
    }

    /**
     * 删除作业附件
     * @param assignmentId 作业ID
     * @param attachmentId 附件ID
     * @param currentUser 当前用户
     * @return 操作状态
     */
    @DeleteMapping("/{assignmentId}/attachments/{attachmentId}")
    @Operation(summary = "删除作业附件",
            description = "删除指定作业的附件文件",
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功"),
                    @ApiResponse(responseCode = "403", description = "无权限操作"),
                    @ApiResponse(responseCode = "404", description = "作业或附件不存在")
            })
    public ResponseEntity<Void> deleteAssignmentAttachment(
            @PathVariable Long assignmentId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal User currentUser) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该作业", HttpStatus.FORBIDDEN);
        }

        // 验证附件存在性
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        
        // 从作业中移除附件
        assignment.getAttachments().removeIf(att -> att.getId().equals(attachmentId));
        assignmentService.updateAssignment(assignment);
        
        // 删除附件
        attachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 下载作业附件
     * @param assignmentId 作业ID
     * @param attachmentId 附件ID
     * @param currentUser 当前用户
     * @return 附件内容
     * @throws IOException 文件读取错误
     */
    @GetMapping("/{assignmentId}/attachments/{attachmentId}/download")
    @Operation(summary = "下载作业附件",
            description = "下载指定作业的附件文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "下载成功"),
                    @ApiResponse(responseCode = "403", description = "无权限操作"),
                    @ApiResponse(responseCode = "404", description = "作业或附件不存在")
            })
    public ResponseEntity<org.springframework.core.io.Resource> downloadAssignmentAttachment(
            @PathVariable Long assignmentId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal User currentUser) throws IOException {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        // 验证教师权限
        Course course = assignment.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该作业", HttpStatus.FORBIDDEN);
        }

        // 获取附件信息
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        
        // 下载附件内容
        byte[] data = attachmentService.downloadAttachment(attachmentId);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .contentLength(attachment.getSize())
                .body(resource);
    }


    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignments(@PathVariable Long courseId,
                                                                      @AuthenticationPrincipal User currentUser) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new ApiException("Course not found", HttpStatus.NOT_FOUND);
        }
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to view this course", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignmentService.getAssignmentsByCourseId(courseId)));
    }

    @GetMapping("/{assignmentId}/testcases")
    @Operation(summary = "Get test cases",
            description = "获取指定作业的所有测试用例")
    public ResponseEntity<List<TestCaseResponseDTO>> getTestCases(@PathVariable Long assignmentId,
                                                                  @AuthenticationPrincipal User currentUser) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            throw new ApiException("Assignment not found", HttpStatus.NOT_FOUND);
        }
        Course course = assignment.getCourse();
        if (course == null) {
            throw new ApiException("Course not found", HttpStatus.NOT_FOUND);
        }
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("No permission to view this assignment", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(
                assignment.getTestCases().stream()
                .map(TestCaseResponseDTO::new)
                .collect(Collectors.toList())
        );
    }
}