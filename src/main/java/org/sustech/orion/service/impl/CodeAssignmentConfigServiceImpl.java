package org.sustech.orion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sustech.orion.dto.CodeAssignmentConfigDTO;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.CodeAssignmentConfig;
import org.sustech.orion.repository.AssignmentRepository;
import org.sustech.orion.repository.CodeAssignmentConfigRepository;
import org.sustech.orion.service.CodeAssignmentConfigService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeAssignmentConfigServiceImpl implements CodeAssignmentConfigService {

    private final CodeAssignmentConfigRepository codeAssignmentConfigRepository;
    private final AssignmentRepository assignmentRepository;

    @Override
    public CodeAssignmentConfig getByAssignmentId(Long assignmentId) {
        return codeAssignmentConfigRepository.findByAssignmentId(assignmentId)
                .orElse(null);
    }

    @Override
    @Transactional
    public CodeAssignmentConfig createOrUpdate(CodeAssignmentConfigDTO configDTO) {
        Assignment assignment = assignmentRepository.findById(configDTO.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("作业不存在: " + configDTO.getAssignmentId()));

        CodeAssignmentConfig config;
        if (configDTO.getId() != null) {
            config = codeAssignmentConfigRepository.findById(configDTO.getId())
                    .orElseThrow(() -> new RuntimeException("代码配置不存在: " + configDTO.getId()));
        } else {
            config = new CodeAssignmentConfig();
            config.setAssignment(assignment);
        }

        // 设置配置属性
        config.setAllowedLanguages(configDTO.getAllowedLanguages());
        config.setMemoryLimitEnabled(configDTO.getMemoryLimitEnabled());
        config.setMemoryLimitMB(configDTO.getMemoryLimitMB());
        config.setTimeLimitEnabled(configDTO.getTimeLimitEnabled());
        config.setTimeLimitSeconds(configDTO.getTimeLimitSeconds());
        return codeAssignmentConfigRepository.save(config);
    }

    @Override
    @Transactional
    public void delete(Long configId) {
        codeAssignmentConfigRepository.deleteById(configId);
    }

    @Override
    public CodeAssignmentConfigDTO toDTO(CodeAssignmentConfig config) {
        if (config == null) {
            return null;
        }
        CodeAssignmentConfigDTO dto = new CodeAssignmentConfigDTO();
        dto.setId(config.getId());
        dto.setAllowedLanguages(config.getAllowedLanguages());
        dto.setMemoryLimitEnabled(config.getMemoryLimitEnabled());
        dto.setMemoryLimitMB(config.getMemoryLimitMB());
        dto.setTimeLimitEnabled(config.getTimeLimitEnabled());
        dto.setTimeLimitSeconds(config.getTimeLimitSeconds());
        dto.setAssignmentId(config.getAssignment().getId());
        return dto;
    }
} 