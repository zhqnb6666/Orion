package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.*;
import org.sustech.orion.repository.*;
import org.sustech.orion.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sustech.orion.dto.CodeSubmissionResult;
import org.sustech.orion.dto.CodeSubmissionDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionConfigRepository submissionConfigRepository;
    private final CodeExecutionResultRepository codeExecutionResultRepository;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository, 
                               AssignmentRepository assignmentRepository, 
                               SubmissionConfigRepository submissionConfigRepository,
                               CodeExecutionResultRepository codeExecutionResultRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionConfigRepository = submissionConfigRepository;
        this.codeExecutionResultRepository = codeExecutionResultRepository;
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

        // 验证状态有效性
        try {
            Submission.SubmissionStatus status =
                    Submission.SubmissionStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid status: " + newStatus, HttpStatus.BAD_REQUEST);
        }

        submission.setStatus(Submission.SubmissionStatus.valueOf(newStatus));
        submissionRepository.save(submission);
    }

    @Override
    public Integer getSubmissionAttempts(Long studentId, Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignment_IdAndStudent_UserIdOrderBySubmitTimeDesc(assignmentId, studentId);
        return submissions.isEmpty() ? 0 : submissions.size();
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
        if (attempts >= submissionConfigRepository.findByAssignmentId(assignmentId).getMaxSubmissionAttempts()) {
            throw new ApiException("Exceeded maximum submission attempts", HttpStatus.FORBIDDEN);
        }

        submission.setAssignment(assignment);
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

        // todo: submission.getStatus()不存在DRAFT状态
        if (!Submission.SubmissionStatus.DRAFT.equals(submission.getStatus())) {
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

        return submission.getStatus().getValue().toString();
    }

    @Override
    public void saveSubmission(Submission submission) {
        submissionRepository.save(submission);
    }

    @Override
    public List<Submission> getPendingSubmissions(Long teacherId) {
        return submissionRepository.findByStatusAndAssignment_Course_Instructor_UserId(
                Submission.SubmissionStatus.PENDING,
                teacherId);
    }

    @Override
    public SubmissionConfig getSubmissionConfigByAssignmentId(Long assignmentId) {
        return submissionConfigRepository.findByAssignmentId(assignmentId);
    }

    @Override
    public CodeSubmissionResult getCodeSubmissionResult(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException("Submission not found", HttpStatus.NOT_FOUND));

        CodeSubmissionResult result = new CodeSubmissionResult();
        result.setSubmissionId(submissionId);
        
        // 获取代码提交内容
        CodeSubmission codeSubmission = submission.getContents().stream()
                .filter(content -> content.getType() == SubmissionContent.ContentType.CODE)
                .findFirst()
                .map(SubmissionContent::getCodeSubmission)
                .orElseThrow(() -> new ApiException("Code submission not found", HttpStatus.NOT_FOUND));
        
        result.setScript(codeSubmission.getScript());
        result.setLanguage(codeSubmission.getLanguage());
        result.setVersionIndex(codeSubmission.getVersionIndex());
        result.setStatus(submission.getStatus().getValue());
        
        // 获取成绩和反馈
        if (submission.getGrade() != null) {
            result.setScore(submission.getGrade().getScore());
            result.setFeedback(submission.getGrade().getFeedback());
        }
        
        // 获取测试用例执行结果
        List<CodeExecutionResult> executionResults = codeExecutionResultRepository.findBySubmission_Id(submissionId);
        List<CodeSubmissionResult.TestCaseResult> testCaseResults = executionResults.stream()
                .map(executionResult -> {
                    CodeSubmissionResult.TestCaseResult testCaseResult = new CodeSubmissionResult.TestCaseResult();
                    TestCase testCase = executionResult.getTestCase();
                    
                    testCaseResult.setTestCaseId(testCase.getId());
                    testCaseResult.setInput(testCase.getInput());
                    testCaseResult.setExpectedOutput(testCase.getExpectedOutput());
                    testCaseResult.setActualOutput(executionResult.getOutput());
                    testCaseResult.setError(executionResult.getError());
                    testCaseResult.setStatusCode(executionResult.getStatusCode());
                    testCaseResult.setMemory(executionResult.getMemory());
                    testCaseResult.setCpuTime(executionResult.getCpuTime());
                    testCaseResult.setCompilationStatus(executionResult.getCompilationStatus());
                    testCaseResult.setExecutionSuccess(executionResult.isExecutionSuccess());
                    testCaseResult.setCompiled(executionResult.isCompiled());
                    testCaseResult.setWeight(testCase.getWeight());
                    
                    // 判断测试用例是否通过
                    testCaseResult.setPassed(executionResult.isExecutionSuccess() && 
                            executionResult.getOutput().trim().equals(testCase.getExpectedOutput().trim()));
                    
                    return testCaseResult;
                })
                .collect(Collectors.toList());
        
        result.setTestCaseResults(testCaseResults);
        return result;
    }

    @Override
    public Map<Long, Submission> getLatestSubmissionsByAssignment(Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignment_IdOrderBySubmitTimeDesc(assignmentId);
        return submissions.stream()
                .collect(Collectors.toMap(
                        s -> s.getStudent().getUserId(),
                        s -> s,
                        (existing, replacement) -> existing
                ));
    }

}