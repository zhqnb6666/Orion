package org.sustech.orion.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@Builder
public class GradeSummaryDTO {
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private long totalCourses;
    private Map<String, Double> courseDistribution; // <课程名称, 平均分>
    private long totalAssignments;
}
