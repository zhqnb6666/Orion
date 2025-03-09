package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class GradeResponseDTO {
    private Long id;
    private String name;
    private String type;
    private Double score;    // 允许为null
    private Double totalPoints;
    private Timestamp dueDate;
    private Timestamp submittedDate;
    private Timestamp gradedDate;
    private String feedback;
    private String appealReason;
    private Timestamp appealTime;
    private String status;   // 枚举值：graded/upcoming等

    // 添加枚举类型
    public enum GradeStatus {
        GRADED, UPCOMING, SUBMITTED, MISSING, APPEALING, APPEALED
    }
}

