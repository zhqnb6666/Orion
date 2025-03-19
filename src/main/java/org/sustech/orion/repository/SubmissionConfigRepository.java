package org.sustech.orion.repository;

import org.sustech.orion.model.SubmissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionConfigRepository extends JpaRepository<SubmissionConfig, Long> {

    // 根据作业ID查找提交配置
    SubmissionConfig findByAssignmentId(Long assignmentId);
}
