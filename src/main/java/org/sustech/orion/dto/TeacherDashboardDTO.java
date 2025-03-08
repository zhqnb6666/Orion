package org.sustech.orion.dto;

import lombok.Getter;
import lombok.Setter;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Submission;

import java.util.List;

@Getter
@Setter
public class TeacherDashboardDTO {
    // 待批改作业列表
    private List<Submission> pendingSubmissions;

    // 课程统计
    private int totalCourses;
    private int activeCourses;

    // 即将截止的作业（一周内）
    private List<Assignment> upcomingAssignments;
}
