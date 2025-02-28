package org.sustech.orion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
