package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;

import java.util.List;

@RestController("studentsCourseController")
@RequestMapping("/api/students/courses")
@Tag(name = "Course API", description = "APIs for course management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    /* useful */
    @PostMapping
    @Operation(summary = "Create course", description = "Create a new course")//tested
    public ResponseEntity<Course> createCourse(@RequestBody CourseDTO request, @AuthenticationPrincipal User currentUser) {
        //system.out.println(currentUser.getUsername());
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setSemester(request.getSemester());
        course.setDescription(request.getDescription());
        course.setIsActive(request.getIsActive());
        course.setCreatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        return ResponseEntity.ok(courseService.createCourse(course, currentUser));
    }

    @GetMapping("/{courseId}")//ok
    @Operation(summary = "Get course details", description = "Get course by ID")
    public ResponseEntity<Course> getCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }




    /* useless */
    @GetMapping("/semester/{semester}")//ok
    @Operation(summary = "List courses by semester")
    public ResponseEntity<List<Course>> getCoursesBySemester(@PathVariable String semester) {
        return ResponseEntity.ok(courseService.getCoursesBySemester(semester));
    }
}