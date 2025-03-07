package org.sustech.orion.service;

import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.SubmissionConfig;

import java.sql.Timestamp;
import java.util.List;

public interface AssignmentService {
    Assignment createAssignment(Assignment assignment, Long courseId);

    List<Assignment> getActiveAssignments(Long courseId);

    List<Assignment> getAssignmentsByType(Long courseId, String type);

    void extendDueDate(Long assignmentId, Timestamp newDueDate);


    List<Assignment> getPendingAssignments(Long studentId);
    List<Assignment> getUpcomingAssignments(Long studentId, int days);

    Submission createSubmission(Long assignmentId, Submission submission);

    List<Submission> getSubmissionsByAssignmentAndStudent(Long assignmentId, Long studentId);


    SubmissionConfig getSubmissionConfigByAssignmentId(Long assignmentId);

    Assignment getAssignmentById(Long assignmentId);
}
