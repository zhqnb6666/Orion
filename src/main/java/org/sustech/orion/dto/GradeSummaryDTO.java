package org.sustech.orion.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class GradeSummaryDTO {
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Map<String, Long> gradeDistribution; // 成绩分布
    private Integer totalAssignments;
    private Integer gradedAssignments;
}
