package org.sustech.orion.repository;

import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 基础查询
    Course findByCourseCode(String courseCode);
    List<Course> findByCourseName(String courseName);
    List<Course> findBySemester(String semester);
    List<Course> findByIsActive(boolean isActive);

    // 关联教师查询
    List<Course> findByInstructor(User instructor);
    List<Course> findByInstructor_UserId(Long instructorId); // 通过教师ID查询

    // 关联学生查询
    List<Course> findByStudents_UserId(Long studentId); // 通过学生ID查询

    // 统计查询
    long countBySemester(String semester);

    // 模糊搜索
    List<Course> findByCourseNameContainingIgnoreCase(String keyword);

    // 组合查询
    List<Course> findByCourseNameAndSemester(String courseName, String semester);
}
