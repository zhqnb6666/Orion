package org.sustech.orion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.model.PlagiarismCheck;
import org.sustech.orion.service.PlagiarismService;

import java.util.List;

@RestController
@RequestMapping("/api/plagiarism")
public class PlagiarismController {

    @Autowired
    private PlagiarismService plagiarismService;

    /**
     * 对两个提交进行查重
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkPlagiarism(
            @RequestParam Long submissionAId,
            @RequestParam Long submissionBId,
            @RequestParam(defaultValue = "qwq-32b") String modelName) {
        
        try {
            PlagiarismCheck result = plagiarismService.checkPlagiarism(submissionAId, submissionBId, modelName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取特定作业的所有查重记录
     */
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getChecksByAssignment(@PathVariable Long assignmentId) {
        try {
            List<PlagiarismCheck> checks = plagiarismService.getChecksByAssignment(assignmentId);
            return ResponseEntity.ok(checks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取特定查重记录的详细信息
     */
    @GetMapping("/{checkId}")
    public ResponseEntity<?> getCheckDetails(@PathVariable Long checkId) {
        try {
            PlagiarismCheck check = plagiarismService.getCheckById(checkId);
            return ResponseEntity.ok(check);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 