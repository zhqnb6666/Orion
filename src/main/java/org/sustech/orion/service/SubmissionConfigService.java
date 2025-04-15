package org.sustech.orion.service;

import org.sustech.orion.model.SubmissionConfig;

public interface SubmissionConfigService {
    /**
     * 根据作业ID获取提交配置
     */
    SubmissionConfig getSubmissionConfigByAssignmentId(Long assignmentId);

    /**
     * 创建或更新提交配置
     */
    SubmissionConfig saveSubmissionConfig(SubmissionConfig config);

    /**
     * 为作业创建默认的提交配置
     */
    SubmissionConfig createDefaultConfig(Long assignmentId);

    /**
     * 删除提交配置
     */
    void deleteSubmissionConfig(Long configId);
} 