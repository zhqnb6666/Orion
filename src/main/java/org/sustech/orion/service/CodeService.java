package org.sustech.orion.service;

public interface CodeService {
    /**
     * 执行并评估代码提交
     * @param submissionId 提交ID
     */
    void executeAndEvaluateSubmission(Long submissionId);
    
    /**
     * 检查代码提交是否符合代码作业配置的限制
     * @param submissionId 提交ID
     * @return 是否符合限制
     */
    boolean validateSubmissionAgainstConfig(Long submissionId);
}