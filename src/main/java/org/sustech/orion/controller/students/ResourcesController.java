package org.sustech.orion.controller.students;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Resource;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.ResourceService;

import java.io.IOException;

@RestController("studentsResourcesController")
@RequestMapping("/api/students/resources")
@Tag(name = "Resources API", description = "APIs for resources management")
public class ResourcesController {

    private final ResourceService resourceService;
    private final CourseService courseService;

    public ResourcesController(ResourceService resourceService, CourseService courseService) {
        this.resourceService = resourceService;
        this.courseService = courseService;
    }

    //TODO:具体下载资源文件的逻辑
    @GetMapping("/{resourceId}/download")
    @Operation(summary = "下载资源文件",
            description = "下载指定资源文件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "文件下载成功"),
                    @ApiResponse(responseCode = "403", description = "无权访问该资源"),
                    @ApiResponse(responseCode = "404", description = "资源不存在")
            })
    public ResponseEntity<byte[]> downloadResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User student) {

        Resource resource = resourceService.getResourceById(resourceId);

        // 权限验证
        if (resource.getCourse() != null &&
                !courseService.isStudentInCourse(resource.getCourse().getId(), student.getUserId())) {
            throw new ApiException("无权访问该课程资源", HttpStatus.FORBIDDEN);
        }

        try {
            byte[] fileContent = resourceService.downloadResourceFile(resource.getUrl());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(fileContent);
        } catch (IOException e) {
            throw new ApiException("文件下载失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

