package org.sustech.orion.service;

import org.sustech.orion.dto.CodeSubmissionResult;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.SubmissionConfig;

import java.util.List;

public interface SubmissionService {
    Submission getSubmission(Long assignmentId, Long studentId);

    List<Submission> getSubmissionsByStatus(String status);

    void updateSubmissionStatus(Long submissionId, String newStatus);

    Integer getSubmissionAttempts(Long studentId, Long assignmentId);

    Submission getSubmissionOrThrow(Long submissionId);

    Submission createSubmission(Long assignmentId, Submission submission);

    List<Submission> getSubmissionsByAssignmentAndStudent(Long assignmentId, Long studentId);

    void deleteStudentSubmission(Long submissionId, Long studentId);

    String getSubmissionStatus(Long submissionId, Long studentId);

    void saveSubmission(Submission submission);

    List<Submission> getPendingSubmissions(Long teacherId);

    SubmissionConfig getSubmissionConfigByAssignmentId(Long assignmentId);

    CodeSubmissionResult getCodeSubmissionResult(Long submissionId);
}
