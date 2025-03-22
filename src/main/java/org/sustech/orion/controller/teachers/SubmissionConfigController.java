package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.model.SubmissionConfig;
import org.sustech.orion.service.SubmissionConfigService;

@RestController
@RequestMapping("/api/teachers/submission-configs")
@Tag(name = "Submission Config API", description = "APIs for managing submission configurations")
public class SubmissionConfigController {

    private final SubmissionConfigService submissionConfigService;

    public SubmissionConfigController(SubmissionConfigService submissionConfigService) {
        this.submissionConfigService = submissionConfigService;
    }

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "获取作业的提交配置")
    public ResponseEntity<SubmissionConfig> getConfig(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionConfigService.getSubmissionConfigByAssignmentId(assignmentId));
    }

    @PostMapping("/assignments/{assignmentId}")//默认无需调用该API
    @Operation(summary = "创建作业的默认提交配置")
    public ResponseEntity<SubmissionConfig> createDefaultConfig(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionConfigService.createDefaultConfig(assignmentId));
    }

    @PutMapping("/{configId}")
    @Operation(summary = "更新提交配置")
    public ResponseEntity<SubmissionConfig> updateConfig(
            @PathVariable Long configId,
            @RequestBody SubmissionConfig config) {
        config.setId(configId);
        return ResponseEntity.ok(submissionConfigService.saveSubmissionConfig(config));
    }

    @DeleteMapping("/{configId}")//默认无需调用该API
    @Operation(summary = "删除提交配置")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long configId) {
        submissionConfigService.deleteSubmissionConfig(configId);
        return ResponseEntity.noContent().build();
    }
} 