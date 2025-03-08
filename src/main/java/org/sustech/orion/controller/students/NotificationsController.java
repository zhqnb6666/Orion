package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.NotificationDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Notification;
import org.sustech.orion.model.User;
import org.sustech.orion.service.NotificationService;
import org.sustech.orion.service.UserService;

import java.util.List;

@RestController("studentsNotificationsController")
@RequestMapping("/api/students/notifications")
@Tag(name = "Notifications API", description = "APIs for notifications management")
public class NotificationsController {

    private final UserService userService;
    // 假设有NotificationService处理通知逻辑
    private final NotificationService notificationService;

    public NotificationsController(UserService userService,
                                   NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // 获取所有通知
    @GetMapping
    @Operation(summary = "获取用户通知列表",
            description = "获取当前用户的所有通知（包含已读/未读）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取通知列表")
            })
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(
            @AuthenticationPrincipal User currentUser) {

        List<NotificationDTO> notifications = notificationService
                .getAllNotifications(currentUser.getUserId());
        return ResponseEntity.ok(notifications);
    }

    // 获取未读通知
    @GetMapping("/unread")
    @Operation(summary = "获取未读通知",
            description = "获取当前用户的未读通知列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取未读通知")
            })
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal User currentUser) {

        List<NotificationDTO> unreadNotifications = notificationService
                .getUnreadNotifications(currentUser.getUserId());
        return ResponseEntity.ok(unreadNotifications);
    }
    // 在NotificationsController.java中添加以下方法

    // 标记通知为已读
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读",
            description = "将指定通知标记为已读状态",
            responses = {
                    @ApiResponse(responseCode = "200", description = "标记成功"),
                    @ApiResponse(responseCode = "403", description = "无权操作该通知"),
                    @ApiResponse(responseCode = "404", description = "通知不存在")
            })
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User currentUser) {

        Notification notification = notificationService.getNotificationById(notificationId);

        // 验证通知所有权
        if (!notification.getRecipient().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权操作该通知", HttpStatus.FORBIDDEN);
        }

        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // 删除通知
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知",
            description = "删除指定通知",
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功"),
                    @ApiResponse(responseCode = "403", description = "无权操作该通知"),
                    @ApiResponse(responseCode = "404", description = "通知不存在")
            })
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User currentUser) {

        Notification notification = notificationService.getNotificationById(notificationId);

        // 验证通知所有权
        if (!notification.getRecipient().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权操作该通知", HttpStatus.FORBIDDEN);
        }

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

}


