package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sustech.orion.util.SemesterUtil;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    @Override
    public Course createCourse(Course course, User instructor) {
        if (courseRepository.findByCourseCode(course.getCourseCode()) != null) {
            throw new ApiException("Course code already exists", HttpStatus.BAD_REQUEST);
        }
        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    @Override
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException("Course not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Course> getCoursesBySemester(String semester) {
        return courseRepository.findBySemester(semester);
    }

    @Override
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructor_UserId(instructorId);
    }

    @Transactional
    @Override
    public void deactivateCourse(Long courseId) {
        Course course = getCourseById(courseId);
        course.setIsActive(false);
        courseRepository.save(course);
    }

    @Override
    public List<Course> getCoursesByStudentId(Long studentId) {
        return courseRepository.findByStudents_UserId(studentId);
    }
    @Override
    public List<Course> getCurrentSemesterCourses(Long studentId) {
        String currentSemester = SemesterUtil.getCurrentSemester(); // 需要实现学期工具类
        return courseRepository.findBySemesterAndStudents_UserId(currentSemester, studentId);
    }

    @Override
    public void addStudentToCourse(Long courseId, User student) {
        Course course = getCourseById(courseId);
        if (course.getStudents().contains(student)) {
            throw new ApiException("您已加入该课程", HttpStatus.BAD_REQUEST);
        }
        course.getStudents().add(student);
        courseRepository.save(course);
    }

    @Override
    public boolean isStudentInCourse(Long courseId, Long userId) {
        return courseRepository.existsByStudents_UserIdAndId(userId, courseId);
    }
}
