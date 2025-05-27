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
    private String courseCode; // 新增课程编号
    private String assignmentType; // 新增作业类型

} 