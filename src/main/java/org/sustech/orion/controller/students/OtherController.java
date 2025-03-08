package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.model.User;
import org.sustech.orion.service.UserService;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.dto.StudentDashboardDTO;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Other API", description = "学生个人信息和仪表盘接口")
public class OtherController {

    private final UserService userService;
    private final AssignmentService assignmentService;

    public OtherController(UserService userService, AssignmentService assignmentService) {
        this.userService = userService;
        this.assignmentService = assignmentService;
    }

    // 更新个人资料
    @PutMapping("/profile")
    @Operation(summary = "更新学生个人资料")
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
                        user.setRole(User.Role.STUDENT); // 邮箱变更需要重新验证
                    }
                }
            }
        });

        user.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        return ResponseEntity.ok(userService.updateUser(user));
    }

    // 获取仪表盘数据
    @GetMapping("/dashboard")
    @Operation(summary = "获取学生仪表盘数据")
    public ResponseEntity<StudentDashboardDTO> getDashboard(
            @AuthenticationPrincipal User currentUser) {

        StudentDashboardDTO dashboard = new StudentDashboardDTO();

        // 待完成作业
        dashboard.setPendingAssignments(
                assignmentService.getPendingAssignments(currentUser.getUserId())
        );

        // 最近截止（3天内）
        dashboard.setUpcomingDeadlines(
                assignmentService.getUpcomingAssignments(currentUser.getUserId(), 3)
        );


        return ResponseEntity.ok(dashboard);
    }
}
