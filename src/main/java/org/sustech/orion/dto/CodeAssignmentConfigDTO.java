package org.sustech.orion.dto;

import lombok.Data;

@Data
public class CodeAssignmentConfigDTO {
    private Long id;
    private String allowedLanguages;
    private Boolean memoryLimitEnabled;
    private Integer memoryLimitMB;
    private Boolean timeLimitEnabled;
    private Integer timeLimitSeconds;
    private Long assignmentId;
} 