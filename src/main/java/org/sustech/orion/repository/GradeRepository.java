package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.Grade;

import java.util.List;
import java.util.Optional;

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

    @Query(
        value = """
                WITH latest_submissions AS (
                  SELECT
                      s.*,
                      ROW_NUMBER() OVER (PARTITION BY s.assignment_id ORDER BY s.submit_time DESC) as rn
                  FROM submissions s
                  JOIN assignments a ON s.assignment_id = a.id
                  WHERE s.student_id = :studentId AND a.course_id = :courseId
                )
                SELECT g.*
                FROM grades g
                JOIN latest_submissions s ON g.submission_id = s.id
                WHERE s.rn = 1
        """,
        nativeQuery = true
    )
    List<Grade> findLatestGradesByStudentAndCourse(Long studentId, Long courseId);

    List<Grade> findBySubmission_Assignment_IdAndSubmission_Student_UserId(Long assignmentId, Long studentId);

    List<Grade> findBySubmission_Student_UserId(Long studentId);

    boolean existsBySubmissionId(Long id);

    Optional<Object> findBySubmissionId(Long id);
}