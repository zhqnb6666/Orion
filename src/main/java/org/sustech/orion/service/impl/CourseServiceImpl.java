package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sustech.orion.util.SemesterUtil;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
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

    @Override
    public Course updateCourse(Course course) {

        return courseRepository.save(course);
    }
    @Override
    public void deleteCourseWithDependencies(Long courseId) {

        courseRepository.deleteById(courseId);
    }
    @Override
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        Course course = getCourseById(courseId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("学生不存在", HttpStatus.NOT_FOUND));

        if (!course.getStudents().contains(student)) {
            throw new ApiException("学生未加入该课程", HttpStatus.BAD_REQUEST);
        }

        course.getStudents().remove(student);
        courseRepository.save(course);
    }
    @Transactional
    @Override
    public Course joinCourseByCode(String courseCode, User student) {
        Course course = courseRepository.findByCourseCode(courseCode);
        if (course == null) {
            throw new ApiException("课程不存在", HttpStatus.NOT_FOUND);
        }
        if (course.getStudents().contains(student)) {
            throw new ApiException("您已加入该课程", HttpStatus.BAD_REQUEST);
        }
        course.getStudents().add(student);
        return courseRepository.save(course);
    }


}
