package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.GradeDTO;
import org.sustech.orion.dto.GradeSummaryDTO;
import org.sustech.orion.dto.responseDTO.GradeResponseDTO;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.User;
import org.sustech.orion.service.GradeService;
import org.sustech.orion.util.ConvertDTO;

import java.util.List;

@RestController("studentsGradeController")
@RequestMapping("/api/students/grades")
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

    @GetMapping("/summary")
    @Operation(summary = "获取成绩汇总",
            description = "获取当前学生的成绩统计概览",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取汇总数据")
            })
    public ResponseEntity<GradeSummaryDTO> getGradeSummary(
            @AuthenticationPrincipal User student) {

        GradeSummaryDTO summary = gradeService.getGradeSummary(student.getUserId());
        return ResponseEntity.ok(summary);
    }





}