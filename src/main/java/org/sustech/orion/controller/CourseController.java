package org.sustech.orion.controller;

import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course API", description = "APIs for course management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @Operation(summary = "Create course", description = "Create a new course")
    public ResponseEntity<Course> createCourse(@RequestBody CourseDTO request, @RequestAttribute User currentUser) {
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setSemester(request.getSemester());
        return ResponseEntity.ok(courseService.createCourse(course, currentUser));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course details", description = "Get course by ID")
    public ResponseEntity<Course> getCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }

    @GetMapping("/semester/{semester}")
    @Operation(summary = "List courses by semester")
    public ResponseEntity<List<Course>> getCoursesBySemester(@PathVariable String semester) {
        return ResponseEntity.ok(courseService.getCoursesBySemester(semester));
    }

    @PatchMapping("/{courseId}/deactivate")
    @Operation(summary = "Deactivate course")
    public ResponseEntity<Void> deactivateCourse(@PathVariable Long courseId) {
        courseService.deactivateCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}