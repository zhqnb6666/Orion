package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.CodeExecutionResult;

import java.util.List;

@Repository
public interface CodeExecutionResultRepository extends JpaRepository<CodeExecutionResult, Long> {
    List<CodeExecutionResult> findBySubmission_Id(Long submissionId);
}
