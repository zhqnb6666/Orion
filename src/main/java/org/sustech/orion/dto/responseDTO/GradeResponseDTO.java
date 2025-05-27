package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;
import org.sustech.orion.model.AIGrading;

import java.sql.Timestamp;

@Getter
@Setter
public class GradeResponseDTO {
    private Long id;
    private String title;
    private String type;
    private Double score;    // 允许为null
    private Integer maxScore;
    private Timestamp dueDate;
    private Timestamp submittedDate;
    private Timestamp gradedDate;
    private String feedback;
    private String appealReason;
    private Timestamp appealTime;
    private String status;   // 枚举值：graded/upcoming等

    private AIGrading aiGrading; // 添加 AI 评分字段
    private Long assignmentId; // 添加作业ID字段

    // 添加枚举类型
    public enum GradeStatus {
        GRADED, UPCOMING, SUBMITTED, MISSING, APPEALING, APPEALED
    }
}

