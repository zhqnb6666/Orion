package org.sustech.orion.service;

import org.sustech.orion.dto.GradeSummaryDTO;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;

import java.util.List;

public interface GradeService {
    Grade gradeSubmission(Long submissionId, Double score, String feedback, User grader);

    List<Grade> getGradesByGrader(Long graderId);

    void finalizeGrade(Long gradeId);

    List<Grade> getGradesByStudentAndCourse(Long studentId, Long courseId);

    List<Grade> getFeedbackForAssignment(Long assignmentId, Long studentId);

    GradeSummaryDTO getGradeSummary(Long studentId);

    void submitGradeAppeal(Long gradeId, String appealReason);
}
