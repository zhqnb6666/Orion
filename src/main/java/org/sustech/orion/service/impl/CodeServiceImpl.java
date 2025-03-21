package org.sustech.orion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sustech.orion.model.*;
import org.sustech.orion.repository.CodeExecutionResultRepository;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.CodeService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.util.CodeRunnerUtil;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeServiceImpl implements CodeService {

    private final SubmissionRepository submissionRepository;
    private final CodeExecutionResultRepository codeExecutionResultRepository;
    private final CodeRunnerUtil codeRunnerUtil;
    private final ObjectMapper objectMapper;
    private final GradeService gradeService;

    

    @Override
    @Transactional
    public void executeAndEvaluateSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在: " + submissionId));
        CodeSubmission codeSubmission = submission.getContents().get(0).getCodeSubmission();

        String script = codeSubmission.getScript();
        String language = codeSubmission.getLanguage();
        Integer versionIndex = codeSubmission.getVersionIndex();
        List<TestCase> testCases = submission.getAssignment().getTestCases();
        double totalScore = 0.0;
        double totalWeight = 0.0;

        for (TestCase testCase : testCases) {
            try {
                // 执行代码
                String rawResponse = codeRunnerUtil.executeCode(
                        script,
                        language,
                        String.valueOf(versionIndex),
                        testCase.getInput()
                );

                // 解析执行结果
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(rawResponse);

                // 创建执行结果对象
                CodeExecutionResult result = new CodeExecutionResult();
                result.setTestCase(testCase);
                result.setSubmission(submission);
                result.setOutput(jsonNode.path("output").asText());
                result.setError(jsonNode.path("error").asText());
                result.setStatusCode(jsonNode.path("statusCode").asInt());
                result.setMemory(jsonNode.path("memory").asText());
                result.setCpuTime(jsonNode.path("cpuTime").asText());
                result.setCompilationStatus(jsonNode.path("compilationStatus").asText());
                result.setExecutionSuccess(jsonNode.path("isExecutionSuccess").asBoolean());
                result.setCompiled(jsonNode.path("isCompiled").asBoolean());

                boolean passed = result.isExecutionSuccess() &&
                        result.getOutput().trim().equals(testCase.getExpectedOutput().trim());
                double weight = testCase.getWeight();
                totalWeight += weight;
                if (passed) {
                    totalScore += weight;
                }

                codeExecutionResultRepository.save(result);

            } catch (IOException e) {
                log.error("执行代码时发生错误", e);
                CodeExecutionResult result = new CodeExecutionResult();
                result.setTestCase(testCase);
                result.setSubmission(submission);
                result.setOutput("");
                result.setError("执行代码时发生错误: " + e.getMessage());
                result.setStatusCode(-1);
                result.setMemory("0");
                result.setCpuTime("0");
                result.setCompilationStatus("FAILED");
                result.setExecutionSuccess(false);
                result.setCompiled(false);

                codeExecutionResultRepository.save(result);
            }
        }

        double finalScore = totalWeight > 0 ? (totalScore / totalWeight) * 100 : 0;
        submissionRepository.save(submission);

        // 创建自动评分的成绩记录
        gradeService.createAutoGrade(submission, finalScore);
    }
}