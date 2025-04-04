package org.sustech.orion.dto;

import lombok.Getter;
import lombok.Setter;
import org.sustech.orion.model.Assignment;

import java.util.List;

@Getter
@Setter
public class StudentDashboardDTO {
    private List<Assignment> pendingAssignments;    // 待完成作业
    private List<Assignment> upcomingDeadlines;     // 即将截止
}
