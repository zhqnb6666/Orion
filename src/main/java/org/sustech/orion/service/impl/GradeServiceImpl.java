package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.GradeRepository;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.GradeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;

    public GradeServiceImpl(GradeRepository gradeRepository,
                            SubmissionRepository submissionRepository) {
        this.gradeRepository = gradeRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Grade gradeSubmission(Long submissionId, Double score, String feedback, User grader) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));

        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setGrader(grader);
        grade.setScore(score);
        grade.setFeedback(feedback);
        return gradeRepository.save(grade);
    }

    @Override
    public List<Grade> getGradesByGrader(Long graderId) {
        return gradeRepository.findByGrader_UserId(graderId);
    }

    @Override
    public void finalizeGrade(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ApiException("Grade not found", HttpStatus.NOT_FOUND));
        grade.setIsFinalized(true);
        gradeRepository.save(grade);
    }
}
