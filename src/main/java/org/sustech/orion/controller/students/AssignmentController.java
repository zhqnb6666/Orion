package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.AssignmentDTO;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.service.AssignmentService;

import java.sql.Timestamp;
import java.util.List;

@RestController("studentsAssignmentController")
@RequestMapping("/api/students/assignments")
@Tag(name = "Assignment API", description = "APIs for assignment management")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }
    /* useful */


    /* useless */
    @GetMapping("/course/{courseId}/active")
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<Assignment>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getActiveAssignments(courseId));
    }
}