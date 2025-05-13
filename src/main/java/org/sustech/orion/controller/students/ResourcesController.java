package org.sustech.orion.controller.students;

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
import java.util.List;
import java.util.stream.Collectors;

@RestController("studentsResourcesController")
@RequestMapping("/api/students/resources")
@Tag(name = "Student Resources API", description = "APIs for Resources access by students")
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
     * 获取课程的所有资源
     * @param courseId 课程ID
     * @param currentUser 当前用户
     * @return 资源列表
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程资源列表",
              description = "获取学生所在课程的所有资源",
              responses = {
                  @ApiResponse(responseCode = "200", description = "获取成功"),
                  @ApiResponse(responseCode = "403", description = "没有参加该课程"),
                  @ApiResponse(responseCode = "404", description = "课程不存在")
              })
    public ResponseEntity<List<ResourceResponseDTO>> getCourseResources(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        
        // 验证课程存在性
        Course course = courseService.getCourseById(courseId);
        
        // 验证学生是否参加了该课程
        if (!courseService.isStudentInCourse(courseId, currentUser.getUserId())) {
            throw new ApiException("没有参加该课程", HttpStatus.FORBIDDEN);
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
        
        // 验证学生是否参加了该课程
        if (!courseService.isStudentInCourse(resource.getCourse().getId(), currentUser.getUserId())) {
            throw new ApiException("没有权限查看该资源", HttpStatus.FORBIDDEN);
        }
        
        // 返回资源及附件信息
        return ResponseEntity.ok(ConvertDTO.toResourceResponseDTO(resource));
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
              description = "下载特定资源的附件文件",
              responses = {
                  @ApiResponse(responseCode = "200", description = "下载成功"),
                  @ApiResponse(responseCode = "403", description = "没有权限下载该附件"),
                  @ApiResponse(responseCode = "404", description = "资源或附件不存在")
              })
    public ResponseEntity<org.springframework.core.io.Resource> downloadResourceAttachment(
            @PathVariable Long resourceId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal User currentUser) throws IOException {
        
        // 获取资源
        Resource resource = resourceService.getResourceById(resourceId);
        
        // 验证学生是否参加了该课程
        if (!courseService.isStudentInCourse(resource.getCourse().getId(), currentUser.getUserId())) {
            throw new ApiException("没有权限下载该附件", HttpStatus.FORBIDDEN);
        }
        
        // 验证附件是否属于该资源
        boolean attachmentBelongsToResource = resource.getAttachments().stream()
                .anyMatch(att -> att.getId().equals(attachmentId));
        
        if (!attachmentBelongsToResource) {
            throw new ApiException("附件不存在或不属于该资源", HttpStatus.NOT_FOUND);
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
}



