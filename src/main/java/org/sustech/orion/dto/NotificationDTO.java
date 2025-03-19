package org.sustech.orion.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.query.results.TableGroupImpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String senderUsername; // new
    private boolean isRead;
    private Timestamp createdAt;
    private String priority; // modified, "High Priority", "Medium Priority", or "Low Priority"
}
