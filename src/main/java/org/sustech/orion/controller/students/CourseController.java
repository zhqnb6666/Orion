package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Resource;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.service.ResourceService;

import java.util.List;

@RestController("studentsCourseController")
@RequestMapping("/api/students/courses")
@Tag(name = "Course API", description = "APIs for course management")
public class CourseController {

    private final CourseService courseService;
    private final GradeService gradeService;
    private final ResourceService resourceService;

    public CourseController(CourseService courseService, GradeService gradeService,ResourceService resourceService) {
        this.courseService = courseService;
        this.gradeService = gradeService;
        this.resourceService = resourceService;
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

    @GetMapping
    @Operation(summary = "获取学生所有课程")
    public ResponseEntity<List<Course>> getStudentCourses(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(courseService.getCoursesByStudentId(currentUser.getUserId()));
    }
    @GetMapping("/current")
    @Operation(summary = "获取当前学期课程")
    public ResponseEntity<List<Course>> getCurrentCourses(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(courseService.getCurrentSemesterCourses(currentUser.getUserId()));
    }
    @PostMapping("/{courseId}/join")
    @Operation(summary = "加入课程")
    public ResponseEntity<Void> joinCourse(@PathVariable Long courseId,
                                           @AuthenticationPrincipal User currentUser) {
        courseService.addStudentToCourse(courseId, currentUser);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/courses/{courseId}")
    @Operation(summary = "获取课程成绩",
            description = "获取当前学生在指定课程中的所有成绩",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取成绩列表"),
                    @ApiResponse(responseCode = "403", description = "未加入该课程")
            })
    public ResponseEntity<List<Grade>> getCourseGrades(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User student) {

        // 验证学生是否属于该课程
        if (!courseService.isStudentInCourse(courseId, student.getUserId())) {
            throw new ApiException("您未加入该课程", HttpStatus.FORBIDDEN);
        }

        List<Grade> grades = gradeService.getGradesByStudentAndCourse(student.getUserId(), courseId);
        return ResponseEntity.ok(grades);
    }
    @GetMapping("/{courseId}/resources")
    @Operation(summary = "获取课程资源",
            description = "获取指定课程的所有学习资源",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取资源列表"),
                    @ApiResponse(responseCode = "403", description = "未加入该课程"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<List<Resource>> getCourseResources(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User student) {

        // 验证课程存在性
        Course course = courseService.getCourseById(courseId);

        // 验证学生权限
        if (!courseService.isStudentInCourse(courseId, student.getUserId())) {
            throw new ApiException("未加入该课程", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(resourceService.getCourseResources(courseId));
    }
}