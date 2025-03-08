package org.sustech.orion.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.Submission;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // 根据作业+学生查询提交记录
    Optional<Submission> findByAssignment_IdAndStudent_UserId(Long assignmentId, Long studentId);

    // 根据状态查询（如"pending"/"graded"）
    List<Submission> findByStatus(String status);

    // 查询某学生的所有提交
    List<Submission> findByStudent_UserId(Long studentId);

    // 使用JPQL实现复杂查询（例如最新3次提交）
    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId " +
            "ORDER BY s.submitTime DESC LIMIT 3")
    List<Submission> findTop3ByAssignment(@Param("assignmentId") Long assignmentId);

    List<Submission> findByAssignment_IdAndStudent_UserIdOrderBySubmitTimeDesc(Long assignmentId, Long studentId);

    List<Submission> findAllByAssignment_IdAndStudent_UserIdOrderBySubmitTimeDesc(
            Long assignmentId,
            Long studentId
    );


    @Query("SELECT s FROM Submission s " +
            "WHERE s.status = 'ACCEPTED' " +
            "AND s.assignment.course.instructor.userId = :teacherId")
    List<Submission> findByStatusAndAssignment_Course_Teacher_UserId(
            @Param("teacherId") Long teacherId);
}