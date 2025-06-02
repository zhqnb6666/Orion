package org.sustech.orion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.service.OCRService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private OCRService ocrService;

    @Autowired
    private AttachmentService attachmentService;
    /**
     * 对指定路径的图片文件进行OCR识别
     */
    @PostMapping("/recognize-path")
    public ResponseEntity<?> recognizeTextFromPath(@RequestParam("attachmentId") Long attachmentId) {
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }
        Path path =  Paths.get(attachment.getUrl());
        String filePath = path.toAbsolutePath().toString();
        System.out.println(filePath);
        try {
            String result = ocrService.recognizeTextFromPath(filePath);
            return ResponseEntity.ok().body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("OCR识别失败: " + e.getMessage());
        }
    }
} 