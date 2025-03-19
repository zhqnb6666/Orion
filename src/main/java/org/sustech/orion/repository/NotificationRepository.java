package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 获取用户所有通知（返回实体）
    List<Notification> findByRecipient_UserId(Long userId);

    // 获取用户未读通知（根据isRead字段过滤）
    List<Notification> findByRecipient_UserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.id = :id")
    void updateReadStatus(@Param("id") Long id, @Param("isRead") boolean isRead);
}
