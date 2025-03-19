package org.sustech.orion.service.impl;

import jakarta.transaction.Transactional;
import org.sustech.orion.dto.NotificationDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Notification;
import org.sustech.orion.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sustech.orion.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;


    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    @Override
    public List<NotificationDTO> getAllNotifications(Long userId) {
        return notificationRepository.findByRecipient_UserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipient_UserIdAndIsReadFalse(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setSenderUsername(notification.getSender().getUsername());
        dto.setContent(notification.getContent());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setPriority(switch (notification.getPriority()) {
            case HIGH -> "High Priority";
            case MEDIUM -> "Medium Priority";
            case LOW -> "Low Priority";
        });
        return dto;
    }
    @Override
    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException("The complaint has been recorded", HttpStatus.NOT_FOUND));
    }

    @Transactional
    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.updateReadStatus(notificationId, true);
    }

    @Transactional
    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

}

