package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.AIGrading;

import java.util.Optional;

@Repository
public interface AIGradingRepository extends JpaRepository<AIGrading, Long> {
    
    /**
     * 根据提交ID查找AI评分记录
     * 
     * @param submissionId 提交ID
     * @return AI评分记录
     */
    Optional<AIGrading> findBySubmissionId(Long submissionId);
    
    /**
     * 根据置信度阈值查找高置信度的AI评分记录
     * 
     * @param confidenceThreshold 置信度阈值
     * @return 高置信度的AI评分记录列表
     */
    java.util.List<AIGrading> findByConfidenceGreaterThanEqual(Double confidenceThreshold);
}