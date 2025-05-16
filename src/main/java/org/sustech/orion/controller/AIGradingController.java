package org.sustech.orion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.model.AIGrading;
import org.sustech.orion.service.AIGradingService;

@RestController
@RequestMapping("/api/ai-grading")
public class AIGradingController {

    @Autowired
    private AIGradingService aiGradingService;

    /**
     * 对特定提交进行AI评分
     */
    @PostMapping("/grade")
    public ResponseEntity<?> gradeSubmission(
            @RequestParam Long submissionId,
            @RequestParam(defaultValue = "qwq-32b") String modelName) {
        
        try {
            AIGrading result = aiGradingService.gradeSubmission(submissionId, modelName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取特定提交的AI评分结果
     */
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<?> getGradingBySubmission(@PathVariable Long submissionId) {
        try {
            AIGrading grading = aiGradingService.getGradingBySubmission(submissionId);
            if (grading == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(grading);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 