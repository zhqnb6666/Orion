package org.sustech.orion.controller;

import org.sustech.orion.dto.GradeDTO;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;
import org.sustech.orion.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/grades")
@Tag(name = "Grade API", description = "APIs for grading management")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/{submissionId}")
    @Operation(summary = "Grade submission")
    public ResponseEntity<Grade> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeDTO request,
            @RequestAttribute User grader) {
        return ResponseEntity.ok(
                gradeService.gradeSubmission(
                        submissionId,
                        request.getScore(),
                        request.getFeedback(),
                        grader
                )
        );
    }

    @GetMapping("/grader/{graderId}")
    @Operation(summary = "List grades by grader")
    public ResponseEntity<List<Grade>> getGradesByGrader(@PathVariable Long graderId) {
        return ResponseEntity.ok(gradeService.getGradesByGrader(graderId));
    }

    @PostMapping("/{gradeId}/finalize")
    @Operation(summary = "Finalize grade")
    public ResponseEntity<Void> finalizeGrade(@PathVariable Long gradeId) {
        gradeService.finalizeGrade(gradeId);
        return ResponseEntity.noContent().build();
    }
}