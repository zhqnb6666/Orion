package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.SubmissionConfig;
import org.sustech.orion.repository.AssignmentRepository;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.SubmissionConfigRepository;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionConfigRepository submissionConfigRepository;


    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 CourseRepository courseRepository, SubmissionRepository submissionRepository, SubmissionConfigRepository submissionConfigRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.submissionRepository = submissionRepository;
        this.submissionConfigRepository = submissionConfigRepository;
    }

    @Override
    public Assignment createAssignment(Assignment assignment, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException("Course not found", HttpStatus.NOT_FOUND));
        assignment.setCourse(course);
        return assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> getActiveAssignments(Long courseId) {
        return assignmentRepository.findByCourse_IdAndDueDateAfter(
                courseId, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public List<Assignment> getAssignmentsByType(Long courseId, String type) {
        return assignmentRepository.findByCourse_IdAndType(courseId, type);
    }

    @Override
    public void extendDueDate(Long assignmentId, Timestamp newDueDate) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND));
        assignment.setDueDate(newDueDate);
        assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> getPendingAssignments(Long studentId) {
        // 获取未提交且未过期的作业
        return assignmentRepository.findPendingAssignments(studentId);
    }

    @Override
    public List<Assignment> getUpcomingAssignments(Long studentId, int days) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp deadline = new Timestamp(now.getTime() + (days * 86400000L)); // days转毫秒
        return assignmentRepository.findUpcomingAssignments(studentId, now, deadline);
    }

    @Override
    public Submission createSubmission(Long assignmentId, Submission submission) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND));

        submission.setAssignment(assignment);

        return submissionRepository.save(submission);
    }



    @Override
    public List<Submission> getSubmissionsByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findAllByAssignment_IdAndStudent_UserIdOrderBySubmitTimeDesc(assignmentId, studentId);
    }

    @Override
    public SubmissionConfig getSubmissionConfigByAssignmentId(Long assignmentId) {
        return submissionConfigRepository.findByAssignmentId(assignmentId);

    }

    @Override
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND));
    }


}