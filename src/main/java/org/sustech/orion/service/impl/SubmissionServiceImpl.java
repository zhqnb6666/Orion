package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Submission;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Submission getSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignment_IdAndStudent_UserId(assignmentId, studentId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Submission> getSubmissionsByStatus(String status) {
        return submissionRepository.findByStatus(status);
    }

    @Override
    public void updateSubmissionStatus(Long submissionId, String newStatus) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));
        submission.setStatus(newStatus);
        submissionRepository.save(submission);
    }

    @Override
    public Integer getSubmissionAttempts(Long studentId, Long assignmentId) {
        return submissionRepository.findByAssignment_IdAndStudent_UserId(assignmentId, studentId)
                .map(Submission::getAttempts)
                .orElse(0);
    }

    @Override
    public Submission getSubmissionOrThrow(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));
    }
}