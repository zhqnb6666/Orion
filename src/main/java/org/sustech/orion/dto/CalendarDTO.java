package org.sustech.orion.dto;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class CalendarDTO {
    private Long id;
    private String title;
    private Timestamp deadline;
    private String courseName;
    private Long courseId;
    private Long assignmentId;
    private String description;
    private String eventType;
} 