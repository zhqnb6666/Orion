package org.sustech.orion;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.Notification;
import org.sustech.orion.repository.NotificationRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class NotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    // 测试获取所有通知
    @Test @Order(1)
    @WithUserDetails("student")
    void getAllNotifications_ShouldReturnInitializedData() throws Exception {
        mockMvc.perform(get("/api/students/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test system Notification"))
                .andExpect(jsonPath("$[1].title").value("Test teacher Notification"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    // 测试获取未读通知
    @Test @Order(2)
    @WithUserDetails("student")
    void getUnreadNotifications_ShouldReturnUnreadOnly() throws Exception {
        mockMvc.perform(get("/api/students/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // 测试标记通知为已读
    @Test @Order(3)
    @WithUserDetails("student")
    void markNotificationAsRead_ShouldUpdateReadStatus() throws Exception {
        // 获取第一个通知
        Notification notification = notificationRepository.findAll().get(0);

        mockMvc.perform(put("/api/students/notifications/{id}/read", notification.getId()))
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertTrue(updated.isRead());
    }



    // 测试无权操作其他用户的通知
    @Test @Order(4)
    @WithUserDetails("teacher") // 尝试用教师身份删除学生通知
    void deleteOthersNotification_ShouldForbid() throws Exception {
        Notification studentNotification = notificationRepository.findAll().get(0);

        mockMvc.perform(delete("/api/students/notifications/{id}", studentNotification.getId()))
                .andExpect(status().isForbidden());
    }

    // 测试删除通知
    @Test @Order(5)
    @WithUserDetails("student")
    void deleteNotification_ShouldRemoveFromDatabase() throws Exception {
        // 获取第二个通知
        Notification notification = notificationRepository.findAll().get(1);

        mockMvc.perform(delete("/api/students/notifications/{id}", notification.getId()))
                .andExpect(status().isNoContent());

        assertFalse(notificationRepository.existsById(notification.getId()));
    }

}
