package org.sustech.orion.service;

import jakarta.transaction.Transactional;
import org.sustech.orion.dto.NotificationDTO;
import org.sustech.orion.model.Notification;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getAllNotifications(Long userId);
    List<NotificationDTO> getUnreadNotifications(Long userId);

    Notification getNotificationById(Long notificationId);

    @Transactional
    void markAsRead(Long notificationId);

    @Transactional
    void deleteNotification(Long notificationId);

    @Transactional
    void saveNotification(Notification notification);

    List<NotificationDTO> getSentNotifications(Long senderId);
}
