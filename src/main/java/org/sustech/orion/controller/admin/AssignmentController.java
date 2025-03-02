package org.sustech.orion.controller.admin;

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

@RestController("adminAssignmentController")
@RequestMapping("/api/admin/assignments")
@Tag(name = "Assignment API", description = "APIs for assignment management")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }
    /* useful */
    @PostMapping("/{courseId}")
    @Operation(summary = "Create assignment")
    public ResponseEntity<Assignment> createAssignment(
            @PathVariable Long courseId,
            @RequestBody AssignmentDTO request) {
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setType(request.getType());
        assignment.setDueDate(request.getDueDate());
        return ResponseEntity.ok(assignmentService.createAssignment(assignment, courseId));
    }

    @GetMapping("/course/{courseId}/active")
    @Operation(summary = "Get active assignments")
    public ResponseEntity<List<Assignment>> getActiveAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getActiveAssignments(courseId));
    }

    @PatchMapping("/{assignmentId}/due-date")
    @Operation(summary = "Extend due date")
    public ResponseEntity<Void> extendDueDate(
            @PathVariable Long assignmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Timestamp newDueDate) {
        assignmentService.extendDueDate(assignmentId, newDueDate);
        return ResponseEntity.noContent().build();
    }
    /* useless */
}