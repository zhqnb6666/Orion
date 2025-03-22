package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.CodeExecutionResult;

import java.util.List;

@Repository
public interface CodeExecutionResultRepository extends JpaRepository<CodeExecutionResult, Long> {
    // 可以添加自定义查询方法
    List<CodeExecutionResult> findBySubmission_Id(Long submissionId);
}
