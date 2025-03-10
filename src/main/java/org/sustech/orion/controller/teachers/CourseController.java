package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.dto.responseDTO.AssignmentResponseDTO;
import org.sustech.orion.dto.responseDTO.CourseItemResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.service.AssignmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.UserService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;

@RestController("teachersCourseController")
@RequestMapping("/api/teachers/courses")
@Tag(name = "Course API", description = "APIs for course management")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final AssignmentService assignmentService;

    public CourseController(CourseService courseService, UserService userService, AssignmentService assignmentService) {
        this.courseService = courseService;
        this.userService = userService;
        this.assignmentService = assignmentService;
    }

    /* useful */
    @PostMapping//ok
    @Operation(summary = "Create course", description = "Create a new course")//tested
    public ResponseEntity<CourseItemResponseDTO> createCourse(@RequestBody CourseDTO request, @AuthenticationPrincipal User currentUser) {
        //system.out.println(currentUser.getUsername());
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setSemester(request.getSemester());
        course.setDescription(request.getDescription());
        course.setIsActive(request.getIsActive());
        course.setCreatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTO(courseService.createCourse(course, currentUser)));
    }

    @GetMapping("/{courseId}")//ok
    @Operation(summary = "Get course details", description = "Get course by ID")
    public ResponseEntity<CourseItemResponseDTO> getCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTO(courseService.getCourseById(courseId)));
    }

    /* useless */

    @GetMapping("/semester/{semester}")//ok
    @Operation(summary = "List courses by semester")
    public ResponseEntity<List<CourseItemResponseDTO>> getCoursesBySemester(@PathVariable String semester) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTOList(courseService.getCoursesBySemester(semester)));
    }

    @GetMapping
    @Operation(summary = "获取教师所有课程", description = "获取当前教师负责的所有课程")
    public ResponseEntity<List<CourseItemResponseDTO>> getTeacherCourses(
            @AuthenticationPrincipal User currentUser) {

        List<Course> courses = courseService.getCoursesByInstructor(currentUser.getUserId());
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTOList(courses));
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "更新课程信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "课程更新成功"),
                    @ApiResponse(responseCode = "403", description = "无修改权限"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<CourseItemResponseDTO> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseDTO request,
            @AuthenticationPrincipal User currentUser) {

        Course existingCourse = courseService.getCourseById(courseId);

        // 验证教师权限
        if (!existingCourse.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限修改该课程", HttpStatus.FORBIDDEN);
        }

        // 更新可修改字段
        existingCourse.setCourseName(request.getCourseName());
        existingCourse.setDescription(request.getDescription());
        existingCourse.setSemester(request.getSemester());
        existingCourse.setIsActive(request.getIsActive());

        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTO(courseService.updateCourse(existingCourse)));
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "删除课程",
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功"),
                    @ApiResponse(responseCode = "403", description = "无操作权限"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseService.getCourseById(courseId);

        // 验证教师权限
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限删除该课程", HttpStatus.FORBIDDEN);
        }

        courseService.deleteCourseWithDependencies(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/students")
    @Operation(summary = "获取课程学生列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取学生列表"),
                    @ApiResponse(responseCode = "403", description = "无查看权限")
            })
    public ResponseEntity<List<User>> getCourseStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseService.getCourseById(courseId);

        // 验证教师权限
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限查看该课程学生", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(course.getStudents());
    }

    @PostMapping("/{courseId}/students")
    @Operation(summary = "添加学生到课程",
            responses = {
                    @ApiResponse(responseCode = "204", description = "添加成功"),
                    @ApiResponse(responseCode = "400", description = "学生已存在"),
                    @ApiResponse(responseCode = "403", description = "无操作权限"),
                    @ApiResponse(responseCode = "404", description = "课程或学生不存在")
            })
    public ResponseEntity<Void> addStudentToCourse(
            @PathVariable Long courseId,
            @RequestParam Long studentId,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseService.getCourseById(courseId);

        // 验证教师权限
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该课程", HttpStatus.FORBIDDEN);
        }

        User student = userService.getUserById(studentId);


        courseService.addStudentToCourse(courseId, student);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{courseId}/students/{studentId}")
    @Operation(summary = "从课程移除学生",
            responses = {
                    @ApiResponse(responseCode = "204", description = "移除成功"),
                    @ApiResponse(responseCode = "400", description = "学生不在课程中"),
                    @ApiResponse(responseCode = "403", description = "无操作权限"),
                    @ApiResponse(responseCode = "404", description = "课程或学生不存在")
            })
    public ResponseEntity<Void> removeStudentFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseService.getCourseById(courseId);

        // 验证教师权限
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该课程", HttpStatus.FORBIDDEN);
        }

        courseService.removeStudentFromCourse(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/assignments")
    @Operation(summary = "获取课程作业列表",
            description = "获取指定课程下的所有作业",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取作业列表"),
                    @ApiResponse(responseCode = "403", description = "无查看权限"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<List<AssignmentResponseDTO>> getCourseAssignments(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseService.getCourseById(courseId);

        // 验证教师权限
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限查看该课程作业", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ConvertDTO.toAssignmentResponseDTOList(assignmentService.getAssignmentsByCourseId(courseId)));
    }
}