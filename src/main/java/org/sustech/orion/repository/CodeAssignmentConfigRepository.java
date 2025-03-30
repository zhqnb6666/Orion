package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.CodeAssignmentConfig;

import java.util.Optional;

@Repository
public interface CodeAssignmentConfigRepository extends JpaRepository<CodeAssignmentConfig, Long> {
    
    /**
     * 根据作业ID查找代码作业配置
     * @param assignmentId 作业ID
     * @return 代码作业配置（可能为空）
     */
    Optional<CodeAssignmentConfig> findByAssignmentId(Long assignmentId);
} 