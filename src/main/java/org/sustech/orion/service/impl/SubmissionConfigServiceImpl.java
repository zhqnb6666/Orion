package org.sustech.orion.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.SubmissionConfig;
import org.sustech.orion.repository.AssignmentRepository;
import org.sustech.orion.repository.SubmissionConfigRepository;
import org.sustech.orion.service.SubmissionConfigService;

@Service
public class SubmissionConfigServiceImpl implements SubmissionConfigService {

    private final SubmissionConfigRepository submissionConfigRepository;
    private final AssignmentRepository assignmentRepository;

    public SubmissionConfigServiceImpl(SubmissionConfigRepository submissionConfigRepository,
                                     AssignmentRepository assignmentRepository) {
        this.submissionConfigRepository = submissionConfigRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public SubmissionConfig getSubmissionConfigByAssignmentId(Long assignmentId) {
        return submissionConfigRepository.findByAssignmentId(assignmentId);
    }

    @Override
    public SubmissionConfig saveSubmissionConfig(SubmissionConfig config) {
        if (config.getAssignment() == null) {
            throw new ApiException("Assignment must be specified", HttpStatus.BAD_REQUEST);
        }
        return submissionConfigRepository.save(config);
    }

    @Override
    public SubmissionConfig createDefaultConfig(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND));

        // 检查是否已存在配置
        SubmissionConfig existingConfig = getSubmissionConfigByAssignmentId(assignmentId);
        if (existingConfig != null) {
            throw new ApiException("Config already exists for this assignment", HttpStatus.BAD_REQUEST);
        }

        // 创建默认配置
        SubmissionConfig config = new SubmissionConfig();
        config.setAssignment(assignment);
        config.setMaxFileSize(10L * 1024 * 1024); // 10MB
        config.setAllowedFileTypes("*"); // 允许所有文件类型
        config.setMaxSubmissionAttempts(114514); // 最大提交次数

        return submissionConfigRepository.save(config);
    }

    @Override
    public void deleteSubmissionConfig(Long configId) {
        if (!submissionConfigRepository.existsById(configId)) {
            throw new ApiException("Config not found", HttpStatus.NOT_FOUND);
        }
        submissionConfigRepository.deleteById(configId);
    }
} 