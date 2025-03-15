package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.dto.responseDTO.CourseBasicInfoResponseDTO;
import org.sustech.orion.dto.responseDTO.CourseItemResponseDTO;
import org.sustech.orion.dto.responseDTO.CourseMaterialResponseDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Resource;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;

@RestController("studentsCourseController")
@RequestMapping("/api/students/courses")
@Tag(name = "Course API", description = "APIs for course management")
public class CourseController {

    private final CourseService courseService;
    private final GradeService gradeService;
    private final ResourceService resourceService;

    public CourseController(CourseService courseService, GradeService gradeService, ResourceService resourceService) {
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
    @Operation(summary = "Get course basic info", description = "Get course by ID")
    public ResponseEntity<CourseBasicInfoResponseDTO> getCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toCourseBasicInfoResponseDTO(courseService.getCourseById(courseId)));
    }

    @GetMapping("/courseItem/{courseId}")//ok
    @Operation(summary = "Get courseItem info", description = "Get course by ID")
    public ResponseEntity<CourseItemResponseDTO> getCourseItem(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTO(courseService.getCourseById(courseId)));
    }


    /* useless */
    @GetMapping("/semester/{semester}")//ok
    @Operation(summary = "List courses by semester")
    public ResponseEntity<List<CourseItemResponseDTO>> getCoursesBySemester(@PathVariable String semester) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTOList(courseService.getCoursesBySemester(semester)));
    }

    @GetMapping
    @Operation(summary = "获取学生所有课程")
    public ResponseEntity<List<CourseItemResponseDTO>> getStudentCourses(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTOList(courseService.getCoursesByStudentId(currentUser.getUserId())));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前学期课程")
    public ResponseEntity<List<CourseItemResponseDTO>> getCurrentCourses(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTOList(courseService.getCurrentSemesterCourses(currentUser.getUserId())));
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
                    @ApiResponse(responseCode = "200", description = "Successfully obtain the score list"),
                    @ApiResponse(responseCode = "403", description = "You are not enrolled in the course")
            })
    public ResponseEntity<List<GradeResponseDTO>> getCourseGrades(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User student) {

        // 验证学生是否属于该课程
        if (!courseService.isStudentInCourse(courseId, student.getUserId())) {
            throw new ApiException("You are not enrolled in the course", HttpStatus.FORBIDDEN);
        }

        List<Grade> grades = gradeService.getGradesByStudentAndCourse(student.getUserId(), courseId);
        return ResponseEntity.ok(ConvertDTO.toGradeResponseDTOList(grades));
    }

    @GetMapping("/{courseId}/resources")
    @Operation(summary = "获取课程资源",
            description = "获取指定课程的所有学习资源",
            responses = {
                    @ApiResponse(responseCode = "200", description = "The resource list is successfully obtained. Procedure"),
                    @ApiResponse(responseCode = "403", description = "Not enrolled in the course"),
                    @ApiResponse(responseCode = "404", description = "Curriculum does not exist")
            })
    public ResponseEntity<List<CourseMaterialResponseDTO>> getCourseResources(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User student) {

        // 验证课程存在性
        Course course = courseService.getCourseById(courseId);

        // 验证学生权限
        if (!courseService.isStudentInCourse(courseId, student.getUserId())) {
            throw new ApiException("Not enrolled in the course", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ConvertDTO.ResourceToCourseMaterialResponseDTOList(resourceService.getCourseResources(courseId)));
    }
    @PostMapping("/join")
    @Operation(summary = "通过课程代码加入课程",
            responses = {
                    @ApiResponse(responseCode = "200", description = "加入成功"),
                    @ApiResponse(responseCode = "400", description = "已加入课程"),
                    @ApiResponse(responseCode = "404", description = "课程不存在")
            })
    public ResponseEntity<Void> joinCourseByCode(
            @RequestParam String courseCode,
            @AuthenticationPrincipal User currentUser) {

        courseService.joinCourseByCode(courseCode, currentUser);
        return ResponseEntity.ok().build();
    }
}