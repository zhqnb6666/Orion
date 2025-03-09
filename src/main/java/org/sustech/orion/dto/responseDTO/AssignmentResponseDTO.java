package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter

public class AssignmentResponseDTO {
    // CourseMaterial部分
    private String id;          // 改为字符串类型
    private String title;
    private String type;
    private String description;
    private List<AttachmentResponseDTO> attachments;

    // Assignment特有
    private Timestamp dueDate;
    private Integer maxScore;
    private String status;      // 枚举值：open/closed/upcoming
    private String instructions;
    private String submissionUrl;
}
