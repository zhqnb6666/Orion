package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.PlagiarismCheck;

import java.util.List;

@Repository
public interface PlagiarismCheckRepository extends JpaRepository<PlagiarismCheck, Long> {
    
    /**
     * 根据作业ID查找所有查重记录
     * 
     * @param assignmentId 作业ID
     * @return 查重记录列表
     */
    List<PlagiarismCheck> findByAssignmentId(Long assignmentId);
    
    /**
     * 根据提交A和提交B查找查重记录
     * 
     * @param submissionAId 提交A的ID
     * @param submissionBId 提交B的ID
     * @return 查重记录列表
     */
    List<PlagiarismCheck> findBySubmissionA_IdAndSubmissionB_Id(Long submissionAId, Long submissionBId);

    // 根据相似度阈值筛选
    List<PlagiarismCheck> findBySimilarityScoreGreaterThan(Double threshold);

    // 根据状态查询（如"pending"/"completed"）
    List<PlagiarismCheck> findByStatus(String status);

    // 联合查询：高相似度且未处理的记录
    List<PlagiarismCheck> findBySimilarityScoreGreaterThanAndStatus(Double threshold, String status);
}