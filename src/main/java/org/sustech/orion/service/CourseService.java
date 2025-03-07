package org.sustech.orion.service;

import org.springframework.transaction.annotation.Transactional;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;

import java.util.List;

public interface CourseService {
    @Transactional
    Course createCourse(Course course, User instructor);

    Course getCourseById(Long courseId);

    List<Course> getCoursesBySemester(String semester);

    List<Course> getCoursesByInstructor(Long instructorId);

    @Transactional
    void deactivateCourse(Long courseId);

    List<Course> getCoursesByStudentId(Long studentId);

    List<Course> getCurrentSemesterCourses(Long studentId);

    @Transactional
    void addStudentToCourse(Long courseId, User student);

    boolean isStudentInCourse(Long courseId, Long userId);
}
