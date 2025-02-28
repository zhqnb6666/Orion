package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sustech.orion.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    // 根据作业查询测试用例
    List<TestCase> findByAssignment_Id(Long assignmentId);

    // 根据权重筛选测试用例
    List<TestCase> findByWeightGreaterThan(Double minWeight);

    // 根据输入/输出模糊匹配（复杂查询需用@Query）
    @Query("SELECT t FROM TestCase t WHERE t.input LIKE %:keyword% OR t.expectedOutput LIKE %:keyword%")
    List<TestCase> findByInputOrOutputContaining(@Param("keyword") String keyword);
}