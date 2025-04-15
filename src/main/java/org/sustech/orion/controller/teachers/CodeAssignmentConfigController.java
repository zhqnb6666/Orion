package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CodeAssignmentConfigDTO;
import org.sustech.orion.model.CodeAssignmentConfig;
import org.sustech.orion.service.CodeAssignmentConfigService;

@RestController
@RequestMapping("/api/teachers/code-config")
@Tag(name = "Code Assignment Config API", description = "教师管理代码作业配置的API")
@RequiredArgsConstructor
public class CodeAssignmentConfigController {

    private final CodeAssignmentConfigService codeAssignmentConfigService;

    @GetMapping("/{assignmentId}")
    @Operation(summary = "获取代码作业配置", description = "根据作业ID获取代码作业配置")
    public ResponseEntity<?> getCodeAssignmentConfig(@PathVariable Long assignmentId) {
        CodeAssignmentConfig config = codeAssignmentConfigService.getByAssignmentId(assignmentId);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(codeAssignmentConfigService.toDTO(config));
    }

    @PostMapping
    @Operation(summary = "创建或更新代码作业配置", description = "创建或更新代码作业配置")
    public ResponseEntity<?> createOrUpdateCodeAssignmentConfig(@RequestBody CodeAssignmentConfigDTO configDTO) {
        CodeAssignmentConfig config = codeAssignmentConfigService.createOrUpdate(configDTO);
        return ResponseEntity.ok(codeAssignmentConfigService.toDTO(config));
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "删除代码作业配置", description = "根据配置ID删除代码作业配置")
    public ResponseEntity<?> deleteCodeAssignmentConfig(@PathVariable Long configId) {
        try {
            codeAssignmentConfigService.delete(configId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除代码作业配置失败: " + e.getMessage());
        }
    }
} 