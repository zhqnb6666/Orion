package org.sustech.orion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sustech.orion.model.*;
import org.sustech.orion.repository.CodeExecutionResultRepository;
import org.sustech.orion.repository.SubmissionRepository;
import org.sustech.orion.service.CodeAssignmentConfigService;
import org.sustech.orion.service.CodeService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.util.CodeRunnerUtil;

import java.io.IOException;
import java.util.Arrays;
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
    private final CodeAssignmentConfigService codeAssignmentConfigService;

    @Override
    @Transactional
    public void executeAndEvaluateSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在: " + submissionId));
        CodeSubmission codeSubmission = submission.getContents().get(0).getCodeSubmission();

        // 验证提交的代码是否符合配置要求
        if (!validateSubmissionAgainstConfig(submissionId)) {
            // 创建执行结果，标记为验证失败
            CodeExecutionResult failedResult = new CodeExecutionResult();
            failedResult.setSubmission(submission);
            failedResult.setOutput("");
            failedResult.setError("代码提交不符合作业配置要求");
            failedResult.setStatusCode(-1);
            failedResult.setMemory("0");
            failedResult.setCpuTime("0");
            failedResult.setCompilationStatus("VALIDATION_FAILED");
            failedResult.setExecutionSuccess(false);
            failedResult.setCompiled(false);
            
            codeExecutionResultRepository.save(failedResult);
            return;
        }

        String script = codeSubmission.getScript();
        String language = codeSubmission.getLanguage();
        Integer versionIndex = codeSubmission.getVersionIndex();
        List<TestCase> testCases = submission.getAssignment().getTestCases();
        double totalScore = 0.0;
        double totalWeight = 0.0;

        // 获取代码作业配置
        CodeAssignmentConfig config = submission.getAssignment().getCodeAssignmentConfig();
        
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

                // 检查资源限制是否超出
                boolean resourceLimitExceeded = false;
                String limitExceededReason = "";
                
                if (config != null) {
                    // 检查内存限制
                    if (config.getMemoryLimitEnabled() && config.getMemoryLimitMB() != null) {
                        try {
                            // 假设内存使用格式为 "123 KB" 或 "1.2 MB"
                            String memoryStr = result.getMemory().toLowerCase();
                            double memoryUsed;
                            
                            if (memoryStr.contains("kb")) {
                                memoryUsed = Double.parseDouble(memoryStr.split("\\s+")[0]) / 1024.0; // 转换为MB
                            } else if (memoryStr.contains("mb")) {
                                memoryUsed = Double.parseDouble(memoryStr.split("\\s+")[0]);
                            } else if (memoryStr.contains("gb")) {
                                memoryUsed = Double.parseDouble(memoryStr.split("\\s+")[0]) * 1024.0; // 转换为MB
                            } else {
                                memoryUsed = 0;
                            }
                            
                            if (memoryUsed > config.getMemoryLimitMB()) {
                                resourceLimitExceeded = true;
                                limitExceededReason = "内存超限: " + memoryUsed + " MB > " + config.getMemoryLimitMB() + " MB";
                            }
                        } catch (Exception e) {
                            log.warn("解析内存使用值时出错: {}", result.getMemory(), e);
                        }
                    }
                    
                    // 检查时间限制
                    if (config.getTimeLimitEnabled() && config.getTimeLimitSeconds() != null) {
                        try {
                            // 假设CPU时间格式为 "0.123 秒" 或 "123 ms" 或 "0.123s"
                            String cpuTimeStr = result.getCpuTime().toLowerCase();
                            double cpuTimeSeconds;
                            
                            if (cpuTimeStr.contains("ms")) {
                                cpuTimeSeconds = Double.parseDouble(cpuTimeStr.split("\\s+")[0]) / 1000.0; // 转换为秒
                            } else if (cpuTimeStr.contains("s") || cpuTimeStr.contains("秒")) {
                                cpuTimeSeconds = Double.parseDouble(cpuTimeStr.split("\\s+")[0]);
                            } else {
                                cpuTimeSeconds = 0;
                            }
                            
                            if (cpuTimeSeconds > config.getTimeLimitSeconds()) {
                                resourceLimitExceeded = true;
                                limitExceededReason = "时间超限: " + cpuTimeSeconds + " 秒 > " + config.getTimeLimitSeconds() + " 秒";
                            }
                        } catch (Exception e) {
                            log.warn("解析CPU时间值时出错: {}", result.getCpuTime(), e);
                        }
                    }
                }
                
                if (resourceLimitExceeded) {
                    result.setExecutionSuccess(false);
                    result.setError(limitExceededReason);
                }

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
        gradeService.createAutoGrade(submission, finalScore);
    }
    
    @Override
    public boolean validateSubmissionAgainstConfig(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在: " + submissionId));
        
        CodeSubmission codeSubmission = submission.getContents().get(0).getCodeSubmission();
        CodeAssignmentConfig config = submission.getAssignment().getCodeAssignmentConfig();

        if (config == null) {
            return true;
        }

        if (config.getAllowedLanguages() != null && !config.getAllowedLanguages().isEmpty()) {
            List<String> allowedLanguages = Arrays.asList(config.getAllowedLanguages().split(","));
            if (!allowedLanguages.contains(codeSubmission.getLanguage())) {
                log.warn("不支持的编程语言: {}, 允许的语言: {}", 
                        codeSubmission.getLanguage(), config.getAllowedLanguages());
                return false;
            }
        }
        
        return true;
    }
}