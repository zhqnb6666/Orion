package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    // 根据提交记录查询评分
    Grade findBySubmission_Id(Long submissionId);

    // 查询某评分员的所有评分
    List<Grade> findByGrader_UserId(Long graderId);

    // 查询未最终确定的评分
    List<Grade> findByIsFinalizedFalse();

    // 分数区间查询（例如查找90分以上的优秀作业）
    List<Grade> findByScoreGreaterThanEqual(Double score);
}