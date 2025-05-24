package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.Notification;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.NotificationService;
import org.sustech.orion.dto.NotificationDTO;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController("teachersNotificationController")
@RequestMapping("/api/teachers/notifications")
@Tag(name = "Teachers Notification API", description = "APIs for teachers to manage notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CourseService courseService;

    public NotificationController(NotificationService notificationService, CourseService courseService) {
        this.notificationService = notificationService;
        this.courseService = courseService;
    }

    @PostMapping("/course/{courseId}")
    @Operation(summary = "发送通知给课程所有学生",
            description = "教师给指定课程的所有学生发送通知",
            responses = {
                    @ApiResponse(responseCode = "200", description = "通知发送成功"),
                    @ApiResponse(responseCode = "403", description = "无权限发送通知"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<Void> sendNotificationToCourse(
            @PathVariable Long courseId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "MEDIUM") Notification.Priority priority,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseService.getCourseById(courseId);
        
        // 验证当前用户是否为课程教师
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        // 获取课程所有学生并发送通知
        List<User> students = course.getStudents();
        for (User student : students) {
            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setRecipient(student);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setPriority(priority);
            notification.setCreatedAt(Timestamp.from(Instant.now()));
            notification.setRead(false);
            notificationService.saveNotification(notification);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/sent")
    @Operation(summary = "获取已发送的通知",
            description = "获取当前教师发送的所有通知",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取通知列表")
            })
    public ResponseEntity<List<NotificationDTO>> getSentNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(notificationService.getSentNotifications(currentUser.getUserId()));
    }
} 