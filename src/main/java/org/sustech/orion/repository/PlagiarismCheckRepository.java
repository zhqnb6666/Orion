package org.sustech.orion.repository;

import org.sustech.orion.model.PlagiarismCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlagiarismCheckRepository extends JpaRepository<PlagiarismCheck, Long> {

    // 根据作业查询查重记录
    List<PlagiarismCheck> findByAssignment_Id(Long assignmentId);

    // 根据相似度阈值筛选
    List<PlagiarismCheck> findBySimilarityScoreGreaterThan(Double threshold);

    // 根据状态查询（如"pending"/"completed"）
    List<PlagiarismCheck> findByStatus(String status);

    // 联合查询：高相似度且未处理的记录
    List<PlagiarismCheck> findBySimilarityScoreGreaterThanAndStatus(Double threshold, String status);
}