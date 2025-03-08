package org.sustech.orion.service.impl;

import org.sustech.orion.dto.GradeSummaryDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.GradeRepository;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.GradeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
    @Override
    public List<Grade> getGradesByStudentAndCourse(Long studentId, Long courseId) {
        return gradeRepository.findBySubmission_Student_UserIdAndSubmission_Assignment_Course_Id(studentId, courseId);
    }
    @Override
    public List<Grade> getFeedbackForAssignment(Long assignmentId, Long studentId) {
        return gradeRepository.findBySubmission_Assignment_IdAndSubmission_Student_UserId(assignmentId, studentId);
    }

    @Override
    public GradeSummaryDTO getGradeSummary(Long studentId) {
        List<Grade> grades = gradeRepository.findBySubmission_Student_UserId(studentId);
        // TODO:统计计算逻辑...
        return new GradeSummaryDTO();
    }

    @Override
    public void submitGradeAppeal(Long gradeId, String appealReason) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ApiException("评分记录不存在", HttpStatus.NOT_FOUND));

        if (grade.getAppealReason() != null) {
            throw new ApiException("已存在申诉记录", HttpStatus.CONFLICT);
        }

        grade.setAppealReason(appealReason);
        grade.setAppealTime(new Timestamp(System.currentTimeMillis()));
        gradeRepository.save(grade);

        // TODO:触发通知逻辑...
    }



}
