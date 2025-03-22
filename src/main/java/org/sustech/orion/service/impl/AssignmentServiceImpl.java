package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.*;
import org.sustech.orion.repository.*;
import org.sustech.orion.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionConfigRepository submissionConfigRepository;
    private final TestCaseRepository testCaseRepository;


    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, TestCaseRepository testCaseRepository,
                                 CourseRepository courseRepository, SubmissionRepository submissionRepository, SubmissionConfigRepository submissionConfigRepository) {
        this.assignmentRepository = assignmentRepository;
        this.testCaseRepository = testCaseRepository;
        this.courseRepository = courseRepository;
        this.submissionRepository = submissionRepository;
        this.submissionConfigRepository = submissionConfigRepository;

    }

    @Override
    public Assignment createAssignment(Assignment assignment, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException("Course not found", HttpStatus.NOT_FOUND));
        assignment.setCourse(course);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // 创建默认的提交配置
        SubmissionConfig config = new SubmissionConfig();
        config.setAssignment(savedAssignment);
        config.setMaxFileSize(10L * 1024 * 1024); // 10MB
        config.setAllowedFileTypes("*"); // 允许所有文件类型
        config.setMaxSubmissionAttempts(114514); // 最大提交次数
        submissionConfigRepository.save(config);

        return savedAssignment;
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
    public List<Assignment> getUpcomingAssignmentsForTeacher(Long teacherId, int days) {
        LocalDateTime now = LocalDateTime.now();
        return assignmentRepository.findByTeacherAndDueDateBetween(
                teacherId,
                now,
                now.plusDays(days)
        );
    }

    @Override
    public List<Assignment> getAssignmentsByCourseId(Long courseId) {
        return assignmentRepository.findByCourse_Id(courseId);
    }
    @Override
    public Assignment updateAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignmentWithDependencies(Long assignmentId) {
        // 删除关联的提交记录、成绩等
        //submissionService.deleteByAssignmentId(assignmentId);
        //gradeService.deleteByAssignmentId(assignmentId);
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    public List<TestCase> getTestCasesByAssignmentId(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND))
                .getTestCases();
    }

    @Override
    public TestCase getTestCaseById(Long testCaseId) {
        return testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ApiException("Test case not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public void updateTestcase(TestCase testcase) {
        if (testcase == null || testcase.getId() == null) {
            throw new ApiException("Invalid test case", HttpStatus.BAD_REQUEST);
        }
        testCaseRepository.findById(testcase.getId())
                .orElseThrow(() -> new ApiException("Test case not found", HttpStatus.NOT_FOUND));
        testCaseRepository.save(testcase);
    }

    @Override
    public void addTestcase(TestCase testcase) {
        if (testcase == null) {
            throw new ApiException("Test case cannot be null", HttpStatus.BAD_REQUEST);
        }
        // Set id to null to ensure it creates a new test case
        testcase.setId(null);
        testCaseRepository.save(testcase);
    }

    @Override
    public void deleteTestcase(Long testcaseId) {
        if (!testCaseRepository.existsById(testcaseId)) {
            throw new ApiException("Test case not found", HttpStatus.NOT_FOUND);
        }
        testCaseRepository.deleteById(testcaseId);
    }


}