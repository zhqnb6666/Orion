package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.dto.ResourceDTO;
import org.sustech.orion.dto.responseDTO.AttachmentResponseDTO;
import org.sustech.orion.dto.responseDTO.ResourceResponseDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.Resource;
import org.sustech.orion.model.User;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.service.CourseService;
import org.sustech.orion.service.ResourceService;
import org.sustech.orion.util.ConvertDTO;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController("teachersResourcesController")
@RequestMapping("/api/teachers/resources")
@Tag(name = "Teacher Resources API", description = "APIs for Resources management")
public class ResourcesController {
    
    private final AttachmentService attachmentService;
    private final CourseService courseService;
    private final ResourceService resourceService;
    
    public ResourcesController(AttachmentService attachmentService, CourseService courseService, ResourceService resourceService) {
        this.attachmentService = attachmentService;
        this.courseService = courseService;
        this.resourceService = resourceService;
    }
    
    /**
     * 创建新的资源（不包括附件）
     * @param courseId 课程ID
     * @param resourceDTO 资源数据传输对象
     * @param currentUser 当前用户
     * @return 创建的资源信息
     */
    @PostMapping("/{courseId}/resources")
    @Operation(summary = "创建课程资源",
              description = "创建一个新的资源，不包括附件",
              responses = {
                  @ApiResponse(responseCode = "200", description = "创建成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作该课程"),
                  @ApiResponse(responseCode = "404", description = "课程不存在")
              })
    public ResponseEntity<ResourceResponseDTO> createResource(
            @PathVariable Long courseId,
            @RequestBody ResourceDTO resourceDTO,
            @AuthenticationPrincipal User currentUser) {
        
        // 验证课程存在性与权限
        Course course = courseService.getCourseById(courseId);
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该课程资源", HttpStatus.FORBIDDEN);
        }
        
        // 创建资源
        org.sustech.orion.model.Resource resource = new org.sustech.orion.model.Resource();
        resource.setName(resourceDTO.getTitle());
        resource.setDescription(resourceDTO.getDescription());
        resource.setType(resourceDTO.getType());
        resource.setCourse(course);
        resource.setUploadedBy(currentUser);
        resource.setUploadTime(Timestamp.from(Instant.now()));
        resource.setAttachments(new ArrayList<>());
        
        // 保存资源
        org.sustech.orion.model.Resource savedResource = resourceService.saveResource(resource);
        
        // 返回资源信息
        return ResponseEntity.ok(ConvertDTO.toResourceResponseDTO(savedResource));
    }
    
    /**
     * 更新资源基本信息
     * @param resourceId 资源ID
     * @param resourceDTO 资源数据传输对象
     * @param currentUser 当前用户
     * @return 更新后的资源信息
     */
    @PutMapping("/{resourceId}")
    @Operation(summary = "更新资源信息",
              description = "更新资源的基本信息，不包括附件",
              responses = {
                  @ApiResponse(responseCode = "200", description = "更新成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作该资源"),
                  @ApiResponse(responseCode = "404", description = "资源不存在")
              })
    public ResponseEntity<ResourceResponseDTO> updateResource(
            @PathVariable Long resourceId,
            @RequestBody ResourceDTO resourceDTO,
            @AuthenticationPrincipal User currentUser) {
        
        // 获取资源
        org.sustech.orion.model.Resource resource = resourceService.getResourceById(resourceId);
        
        // 验证权限
        Course course = resource.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该资源", HttpStatus.FORBIDDEN);
        }
        
        // 更新资源信息
        resource.setName(resourceDTO.getTitle());
        resource.setDescription(resourceDTO.getDescription());
        resource.setType(resourceDTO.getType());
        
        // 保存资源
        org.sustech.orion.model.Resource updatedResource = resourceService.saveResource(resource);
        
        // 返回资源信息
        return ResponseEntity.ok(ConvertDTO.toResourceResponseDTO(updatedResource));
    }
    
    /**
     * 删除资源
     * @param resourceId 资源ID
     * @param currentUser 当前用户
     * @return 操作状态
     */
    @DeleteMapping("/{resourceId}")
    @Operation(summary = "删除资源",
              description = "删除资源及其所有附件",
              responses = {
                  @ApiResponse(responseCode = "204", description = "删除成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作该资源"),
                  @ApiResponse(responseCode = "404", description = "资源不存在")
              })
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User currentUser) {
        
        // 获取资源
        org.sustech.orion.model.Resource resource = resourceService.getResourceById(resourceId);
        
        // 验证权限
        Course course = resource.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该资源", HttpStatus.FORBIDDEN);
        }
        
        // 删除资源
        resourceService.deleteResource(resourceId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 为资源添加附件
     *
     * @param resourceId  资源ID
     * @param files        文件
     * @param currentUser 当前用户
     * @return 附件信息
     */
    @PostMapping(value = "/{resourceId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "为资源添加附件",
              description = "上传附件并关联到指定资源",
              responses = {
                  @ApiResponse(responseCode = "200", description = "添加成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作该资源"),
                  @ApiResponse(responseCode = "404", description = "资源不存在")
              })
    public ResponseEntity<List<AttachmentResponseDTO>> addResourceAttachment(
            @PathVariable Long resourceId,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal User currentUser) {
        
        // 获取资源
        org.sustech.orion.model.Resource resource = resourceService.getResourceById(resourceId);
        
        // 验证权限
        Course course = resource.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该资源", HttpStatus.FORBIDDEN);
        }
        
        // 上传附件
        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            Attachment attachment = attachmentService.uploadAttachment(file, Attachment.AttachmentType.Resource);
            attachments.add(attachment);
        }

        // Add attachments to resource
        if (resource.getAttachments() == null) {
            resource.setAttachments(new ArrayList<>());
        }
        resource.getAttachments().addAll(attachments);
        
        // 保存资源
        resourceService.saveResource(resource);
        
        // 返回附件信息
        return ResponseEntity.ok(ConvertDTO.toAttachmentResponseDTOList(attachments));
    }
    
    /**
     * 删除资源附件
     * @param resourceId 资源ID
     * @param attachmentId 附件ID
     * @param currentUser 当前用户
     * @return 操作状态
     */
    @DeleteMapping("/{resourceId}/attachments/{attachmentId}")
    @Operation(summary = "删除资源附件",
              description = "从资源中删除指定附件",
              responses = {
                  @ApiResponse(responseCode = "204", description = "删除成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作该资源"),
                  @ApiResponse(responseCode = "404", description = "资源或附件不存在")
              })
    public ResponseEntity<Void> deleteResourceAttachment(
            @PathVariable Long resourceId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal User currentUser) {
        
        // 获取资源
        org.sustech.orion.model.Resource resource = resourceService.getResourceById(resourceId);
        
        // 验证权限
        Course course = resource.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该资源", HttpStatus.FORBIDDEN);
        }
        
        // 从资源中移除附件
        resource.getAttachments().removeIf(att -> att.getId().equals(attachmentId));
        resourceService.saveResource(resource);
        
        // 删除附件
        attachmentService.deleteAttachment(attachmentId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 下载资源附件
     * @param resourceId 资源ID
     * @param attachmentId 附件ID
     * @param currentUser 当前用户
     * @return 附件内容
     * @throws IOException 文件读取错误
     */
    @GetMapping("/{resourceId}/attachments/{attachmentId}/download")
    @Operation(summary = "下载资源附件",
              description = "下载指定资源的特定附件文件",
              responses = {
                  @ApiResponse(responseCode = "200", description = "下载成功"),
                  @ApiResponse(responseCode = "403", description = "无权限操作该资源"),
                  @ApiResponse(responseCode = "404", description = "资源或附件不存在")
              })
    public ResponseEntity<org.springframework.core.io.Resource> downloadResourceAttachment(
            @PathVariable Long resourceId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal User currentUser) throws IOException {
        
        // 获取资源
        org.sustech.orion.model.Resource resource = resourceService.getResourceById(resourceId);
        
        // 验证权限（如果是教师）
        Course course = resource.getCourse();
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限操作该资源", HttpStatus.FORBIDDEN);
        }
        
        // 获取附件信息
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        
        // 下载附件内容
        byte[] data = attachmentService.downloadAttachment(attachmentId);
        ByteArrayResource byteResource = new ByteArrayResource(data);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .contentLength(attachment.getSize())
                .body(byteResource);
    }

    /**
     * 获取课程的所有资源
     * @param courseId 课程ID
     * @param currentUser 当前用户
     * @return 资源列表
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程资源列表",
              description = "获取指定课程的所有资源",
              responses = {
                  @ApiResponse(responseCode = "200", description = "获取成功"),
                  @ApiResponse(responseCode = "403", description = "无权限查看该课程"),
                  @ApiResponse(responseCode = "404", description = "课程不存在")
              })
    public ResponseEntity<List<ResourceResponseDTO>> getCourseResources(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        
        // 验证课程存在性与权限
        Course course = courseService.getCourseById(courseId);
        if (!course.getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("无权限查看该课程资源", HttpStatus.FORBIDDEN);
        }
        
        // 获取课程资源
        List<Resource> resources = resourceService.getCourseResources(courseId);
        
        return ResponseEntity.ok(ConvertDTO.toResourceResponseDTOList(resources));
    }

    /**
     * 获取资源详情及附件列表
     * @param resourceId 资源ID
     * @param currentUser 当前用户
     * @return 资源及其附件列表
     */
    @GetMapping("/{resourceId}")
    @Operation(summary = "获取资源详情",
            description = "获取特定资源的详细信息及其所有附件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "403", description = "没有权限查看该资源"),
                    @ApiResponse(responseCode = "404", description = "资源不存在")
            })
    public ResponseEntity<ResourceResponseDTO> getResourceDetails(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User currentUser) {

        // 获取资源
        Resource resource = resourceService.getResourceById(resourceId);

        // 验证权限
        if (!resource.getCourse().getInstructor().getUserId().equals(currentUser.getUserId())) {
            throw new ApiException("没有权限查看该资源", HttpStatus.FORBIDDEN);
        }

        // 返回资源及附件信息
        return ResponseEntity.ok(ConvertDTO.toResourceResponseDTO(resource));
    }
}

