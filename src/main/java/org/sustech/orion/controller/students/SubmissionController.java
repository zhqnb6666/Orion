package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.SubmissionDTO;
import org.sustech.orion.model.Submission;
import org.sustech.orion.service.SubmissionService;

import java.util.List;

@RestController("studentsSubmissionController")
@RequestMapping("/api/students/submissions")
@Tag(name = "Submission API", description = "APIs for assignment submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }
    /* useful */
    @GetMapping("/{submissionId}")
    @Operation(summary = "Get submission details")
    public ResponseEntity<Submission> getSubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getSubmissionOrThrow(submissionId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List submissions by status")
    public ResponseEntity<List<Submission>> getSubmissionsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(submissionService.getSubmissionsByStatus(status));
    }

    @PostMapping("/{submissionId}/status")
    @Operation(summary = "Update submission status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long submissionId,
            @RequestBody SubmissionDTO request) {
        submissionService.updateSubmissionStatus(submissionId, request.getNewStatus());
        return ResponseEntity.noContent().build();
    }
    /* useless */
}
