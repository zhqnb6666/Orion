package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sustech.orion.model.Assignment;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // 根据课程查询作业
    List<Assignment> findByCourse_Id(Long courseId);

    // 根据课程和类型查询
    List<Assignment> findByCourse_IdAndType(Long courseId, String type);

    // 时间范围查询（截止日期在[start, end]之间的作业）
    List<Assignment> findByDueDateBetween(Timestamp start, Timestamp end);

    // 统计某课程的作业数量
    long countByCourse_Id(Long courseId);

    List<Assignment> findByCourse_IdAndDueDateAfter(Long courseId, Timestamp timestamp);

    // 待完成作业（未提交且未截止）
    @Query("SELECT a FROM Assignment a " +
            "WHERE a.course IN (SELECT c FROM Course c JOIN c.students s WHERE s.userId = :studentId) " +
            "AND a.dueDate > CURRENT_TIMESTAMP " +
            "AND NOT EXISTS (SELECT s FROM Submission s WHERE s.assignment = a AND s.student.userId = :studentId)")
    List<Assignment> findPendingAssignments(@Param("studentId") Long studentId);

    // 即将截止的作业（未来X天内）
    @Query("SELECT a FROM Assignment a " +
            "WHERE a.course IN (SELECT c FROM Course c JOIN c.students s WHERE s.userId = :studentId) " +
            "AND a.dueDate BETWEEN :start AND :end " +
            "ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingAssignments(@Param("studentId") Long studentId,
                                             @Param("start") Timestamp start,
                                             @Param("end") Timestamp end);
}
