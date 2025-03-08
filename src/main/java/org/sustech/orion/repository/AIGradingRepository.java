package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sustech.orion.model.AIGrading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AIGradingRepository extends JpaRepository<AIGrading, Long> {

    // 根据提交记录查询AI评分
    AIGrading findBySubmission_Id(Long submissionId);

    // 根据置信度筛选可信评分
    List<AIGrading> findByConfidenceGreaterThan(Double minConfidence);

    // 根据分数区间查询（例如查找AI评分在80-100之间的记录）
    @Query("SELECT a FROM AIGrading a WHERE a.aiScore BETWEEN :min AND :max")
    List<AIGrading> findByAiScoreRange(@Param("min") Double min, @Param("max") Double max);
}