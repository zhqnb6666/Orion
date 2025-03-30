package org.sustech.orion.service;

import org.sustech.orion.dto.CodeAssignmentConfigDTO;
import org.sustech.orion.model.CodeAssignmentConfig;

public interface CodeAssignmentConfigService {

    /**
     * 根据作业ID获取代码作业配置
     * @param assignmentId 作业ID
     * @return 代码作业配置
     */
    CodeAssignmentConfig getByAssignmentId(Long assignmentId);

    /**
     * 创建或更新代码作业配置
     * @param configDTO 代码作业配置DTO
     * @return 创建或更新后的代码作业配置
     */
    CodeAssignmentConfig createOrUpdate(CodeAssignmentConfigDTO configDTO);

    /**
     * 删除代码作业配置
     * @param configId 配置ID
     */
    void delete(Long configId);

    /**
     * 将模型对象转换为DTO
     * @param config 代码作业配置模型
     * @return 代码作业配置DTO
     */
    CodeAssignmentConfigDTO toDTO(CodeAssignmentConfig config);
} 