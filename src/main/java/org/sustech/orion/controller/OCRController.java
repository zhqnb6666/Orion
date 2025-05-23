package org.sustech.orion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.service.OCRService;

import java.io.IOException;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private OCRService ocrService;
    /**
     * 对指定路径的图片文件进行OCR识别
     */
    @PostMapping("/recognize-path")
    public ResponseEntity<?> recognizeTextFromPath(@RequestParam("filePath") String filePath) {
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