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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // 检查是否已存在评分记录
        Grade existingGrade = gradeRepository.findBySubmission_Id(submissionId);
        if (existingGrade != null) {
            // 如果已有评分且当前评分更高，则更新评分
            if (score > existingGrade.getScore()) {
                existingGrade.setScore(score);
                existingGrade.setFeedback(feedback);
                existingGrade.setGrader(grader);
                existingGrade.setGradedTime(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
                existingGrade.setIsFinalized(false);//
                return gradeRepository.save(existingGrade);
            }
            // 如果当前评分不高于已有评分，则不做修改
            return existingGrade;
        }

        // 如果没有评分记录，则创建新的评分
        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setGrader(grader);
        grade.setScore(score);
        grade.setFeedback(feedback);
        grade.setGradedTime(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        grade.setIsFinalized(false);
        grade.setStatus(Grade.Status.GRADED);
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

        // 基础统计
        DoubleSummaryStatistics stats = grades.stream()
                .filter(g -> g.getIsFinalized() && g.getScore() != null)
                .mapToDouble(Grade::getScore)
                .summaryStatistics();

        // 课程分布统计
        Map<String, Double> courseDistribution = grades.stream()
                .filter(g -> g.getIsFinalized() && g.getScore() != null)
                .collect(Collectors.groupingBy(
                        g -> g.getSubmission().getAssignment().getCourse().getCourseName(),
                        Collectors.averagingDouble(Grade::getScore)
                ));



        // 构造DTO
        return GradeSummaryDTO.builder()
                .averageScore(stats.getAverage())
                .highestScore(stats.getMax())
                .lowestScore(stats.getMin())
                .totalCourses((long) courseDistribution.size())
                .courseDistribution(courseDistribution)
                .totalAssignments((long) grades.size())
                .build();
    }


    @Override
    public void submitGradeAppeal(Long gradeId, String appealReason) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ApiException("The score record does not exist", HttpStatus.NOT_FOUND));

        if (grade.getAppealReason() != null) {
            throw new ApiException("The complaint has been recorded", HttpStatus.CONFLICT);
        }

        grade.setAppealReason(appealReason);
        grade.setAppealTime(new Timestamp(System.currentTimeMillis()));
        gradeRepository.save(grade);

        // TODO:触发通知逻辑...
    }

    @Override
    public Grade createAutoGrade(Submission submission, Double score) {
        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setGrader(null);  // 自动评分没有具体的评分人
        grade.setScore(score);
        grade.setFeedback("自动评分结果");
        grade.setGradedTime(new Timestamp(System.currentTimeMillis()));
        grade.setIsFinalized(false);
        grade.setStatus(Grade.Status.GRADED);
        return gradeRepository.save(grade);
    }

}
