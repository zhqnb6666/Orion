package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.GradeDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;

@RestController("teachersGradeController")
@RequestMapping("/api/teachers/grades")
@Tag(name = "Grade API", description = "APIs for grading management")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }
    /* useful */

    @PostMapping("/{gradeId}/finalize") //
    @Operation(summary = "Finalize grade")
    public ResponseEntity<Void> finalizeGrade(@PathVariable Long gradeId) {
        gradeService.finalizeGrade(gradeId);
        return ResponseEntity.noContent().build();
    }

    /* useless */
    @PostMapping("/{submissionId}")
    @Operation(summary = "Grade submission")
    public ResponseEntity<GradeResponseDTO> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeDTO request,
            @AuthenticationPrincipal User grader) {
        return ResponseEntity.ok(ConvertDTO.toGradeResponseDTO(
                gradeService.gradeSubmission(
                        submissionId,
                        request.getScore(),
                        request.getFeedback(),
                        grader
                )
        ));
    }

    @GetMapping("/grader/{graderId}")
    @Operation(summary = "List grades by grader")
    public ResponseEntity<List<GradeResponseDTO>> getGradesByGrader(@PathVariable Long graderId) {
        return ResponseEntity.ok(ConvertDTO.toGradeResponseDTOList(gradeService.getGradesByGrader(graderId)));
    }
}