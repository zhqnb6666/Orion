package org.sustech.orion.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.dto.responseDTO.CourseItemResponseDTO;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;

@RestController("adminCourseController")
@RequestMapping("/api/admin/courses")
@Tag(name = "Course API", description = "APIs for course management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    /* useful */


    @GetMapping("/{courseId}")
    @Operation(summary = "Get course details", description = "Get course by ID")
    public ResponseEntity<CourseItemResponseDTO> getCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTO(courseService.getCourseById(courseId)));
    }


    /* useless */
    @GetMapping("/semester/{semester}")
    @Operation(summary = "List courses by semester")
    public ResponseEntity<List<CourseItemResponseDTO>> getCoursesBySemester(@PathVariable String semester) {
        return ResponseEntity.ok(ConvertDTO.toCourseItemResponseDTOList(courseService.getCoursesBySemester(semester)));
    }

}