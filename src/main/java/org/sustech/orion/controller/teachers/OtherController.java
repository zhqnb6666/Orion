package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.TeacherDashboardDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.SubmissionService;
import org.sustech.orion.service.UserService;

import java.util.List;
import java.util.Map;

@RestController("teachersOtherController")
@RequestMapping("/api/teachers")
@Tag(name = "Teachers Other API", description = "APIs Other management")
public class OtherController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final SubmissionService submissionService;
    private final UserService userService;

    // 构造函数注入
    public OtherController(AssignmentService assignmentService,
                           CourseService courseService,
                           SubmissionService submissionService, UserService userService) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.submissionService = submissionService;
        this.userService = userService;
    }
    @PutMapping("/profile")
    @Operation(summary = "更新教师个人资料，支持更新头像、简介、邮箱",
            description = "更新教师个人资料，支持更新头像、简介、邮箱",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功更新个人资料"),
                    @ApiResponse(responseCode = "403", description = "无教师权限")
            })
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> updateRequest) {

        User user = userService.getUserById(currentUser.getUserId());

        // 更新允许修改的字段
        updateRequest.forEach((key, value) -> {
            switch (key) {
                case "avatarUrl" -> user.setAvatarUrl(value);
                case "bio" -> user.setBio(value);
                case "email" -> {
                    if (!user.getEmail().equals(value)) {
                        user.setEmail(value);
                        user.setRole(User.Role.TEACHER); // 邮箱变更需要重新验证
                    }
                }
            }
        });

        user.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取教师仪表盘数据",
            description = "获取待批改作业、课程概览等信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取仪表盘数据"),
                    @ApiResponse(responseCode = "403", description = "无教师权限")
            })
    public ResponseEntity<TeacherDashboardDTO> getTeacherDashboard(
            @AuthenticationPrincipal User currentUser) {

        // 验证教师身份
        if (!currentUser.getRole().equals("TEACHER")) {
            throw new ApiException("无权限访问", HttpStatus.FORBIDDEN);
        }

        TeacherDashboardDTO dashboard = new TeacherDashboardDTO();

        // 待批改作业
        dashboard.setPendingSubmissions(
                submissionService.getPendingSubmissions(currentUser.getUserId())
        );

        // 课程概览
        List<Course> courses = courseService.getCoursesByInstructor(currentUser.getUserId());
        dashboard.setTotalCourses(courses.size());
        dashboard.setActiveCourses(
                (int) courses.stream().filter(Course::getIsActive).count()
        );

        // 最近需要关注的作业（截止时间在一周内）
        dashboard.setUpcomingAssignments(
                assignmentService.getUpcomingAssignmentsForTeacher(currentUser.getUserId(), 7)
        );

        return ResponseEntity.ok(dashboard);
    }
}


