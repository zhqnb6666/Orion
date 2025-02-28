package org.sustech.orion.service;

import org.sustech.orion.model.Submission;

import java.util.List;

public interface SubmissionService {
    Submission getSubmission(Long assignmentId, Long studentId);

    List<Submission> getSubmissionsByStatus(String status);

    void updateSubmissionStatus(Long submissionId, String newStatus);

    Integer getSubmissionAttempts(Long studentId, Long assignmentId);

    Submission getSubmissionOrThrow(Long submissionId);
}
