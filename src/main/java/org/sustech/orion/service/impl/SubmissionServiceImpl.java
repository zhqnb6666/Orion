package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Submission;
import org.sustech.orion.repository.AssignmentRepository;
import org.sustech.orion.repository.SubmissionConfigRepository;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionConfigRepository submissionConfigRepository;


    public SubmissionServiceImpl(SubmissionRepository submissionRepository, AssignmentRepository assignmentRepository, SubmissionConfigRepository submissionConfigRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionConfigRepository = submissionConfigRepository;
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
        submission.setStatus(Submission.SubmissionStatus.valueOf(newStatus));
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
    @Override
    public Submission createSubmission(Long assignmentId, Submission submission) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND));

        // 检查剩余提交次数
        int attempts = getSubmissionAttempts(submission.getStudent().getUserId(), assignmentId);
        if(attempts >= submissionConfigRepository.findByAssignmentId(assignmentId).getMaxSubmissionAttempts()) {
            throw new ApiException("Exceeded maximum submission attempts", HttpStatus.FORBIDDEN);
        }

        submission.setAssignment(assignment);
        submission.setAttempts(attempts + 1);
        return submissionRepository.save(submission);
    }

    @Override
    public List<Submission> getSubmissionsByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignment_IdAndStudent_UserIdOrderBySubmitTimeDesc(assignmentId, studentId);
    }
    @Override
    public void deleteStudentSubmission(Long submissionId, Long studentId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));

        if (!submission.getStudent().getUserId().equals(studentId)) {
            throw new ApiException("Unauthorized to delete this submission", HttpStatus.FORBIDDEN);
        }

        if (!"DRAFT".equals(submission.getStatus())) {
            throw new ApiException("Only DRAFT submissions can be deleted", HttpStatus.BAD_REQUEST);
        }

        submissionRepository.delete(submission);
    }
    @Override
    public String getSubmissionStatus(Long submissionId, Long studentId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));

        if (!submission.getStudent().getUserId().equals(studentId)) {
            throw new ApiException("Unauthorized to view this submission", HttpStatus.FORBIDDEN);
        }

        return submission.getStatus().toString();
    }
    @Override
    public void saveSubmission(Submission submission) {
        submissionRepository.save(submission);
    }

    @Override
    public List<Submission> getPendingSubmissions(Long teacherId) {
        return submissionRepository.findByStatusAndAssignment_Course_Teacher_UserId(
                teacherId);
    }
}