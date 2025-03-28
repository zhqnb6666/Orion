package org.sustech.orion.controller.common;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * 上传附件
     * @param file 文件
     * @param attachmentType 附件类型
     * @return 附件信息
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传附件")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String attachmentType) {

        Attachment.AttachmentType type;
        try {
            type = Attachment.AttachmentType.valueOf(attachmentType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Attachment attachment = attachmentService.uploadAttachment(file, type);
        return ResponseEntity.ok(convertToMap(attachment));
    }

    /**
     * 根据ID下载附件
     * @param id 附件ID
     * @return 文件内容
     * @throws IOException 文件读取错误
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) throws IOException {
        Attachment attachment = attachmentService.getAttachmentById(id);
        byte[] data = attachmentService.downloadAttachment(id);

        ByteArrayResource resource = new ByteArrayResource(data);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .contentLength(attachment.getSize())
                .body(resource);
    }

    /**
     * 获取附件信息
     * @param id 附件ID
     * @return 附件信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAttachment(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(convertToMap(attachment));
    }

    /**
     * 删除附件
     * @param id 附件ID
     * @return 操作状态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取指定类型的附件列表
     * @param type 附件类型
     * @return 附件列表
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAttachmentsByType(@RequestParam("type") String type) {
        Attachment.AttachmentType attachmentType;
        try {
            attachmentType = Attachment.AttachmentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Attachment> attachments = attachmentService.getAttachmentsByType(attachmentType);
        List<Map<String, Object>> attachmentMaps = attachments.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(attachmentMaps);
    }

    private Map<String, Object> convertToMap(Attachment attachment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", attachment.getId());
        map.put("name", attachment.getName());
        map.put("mimeType", attachment.getMimeType());
        map.put("size", attachment.getSize());
        map.put("uploadedAt", attachment.getUploadedAt());
        map.put("type", attachment.getAttachmentType().name());
        return map;
    }
} 