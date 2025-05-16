package org.sustech.orion.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.AI.OCR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class OCRService {

    /**
     * 对上传的图片文件进行OCR识别
     *
     * @param file 上传的图片文件
     * @return OCR识别结果
     * @throws IOException 如果文件处理过程中出错
     */
    public String recognizeText(MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 创建临时文件
        Path tempDir = Files.createTempDirectory("ocr_temp");
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String tempFilename = UUID.randomUUID() + extension;
        Path tempFile = tempDir.resolve(tempFilename);

        // 保存上传的文件到临时目录
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        try {
            // 调用OCR工具进行文本识别
            String recognizedText = OCR.OCR(tempFile.toString());
            
            // 如果识别失败，抛出异常
            if (recognizedText == null || recognizedText.isEmpty()) {
                throw new RuntimeException("OCR识别失败");
            }
            
            return recognizedText;
        } finally {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
                Files.deleteIfExists(tempDir);
            } catch (IOException e) {
                // 记录但不抛出异常
                System.err.println("清理临时文件失败: " + e.getMessage());
            }
        }
    }

    /**
     * 对特定路径的图片文件进行OCR识别
     *
     * @param filePath 图片文件路径
     * @return OCR识别结果
     */
    public String recognizeTextFromPath(String filePath) {
        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        // 调用OCR工具进行文本识别
        String recognizedText = OCR.OCR(filePath);
        
        // 如果识别失败，抛出异常
        if (recognizedText == null || recognizedText.isEmpty()) {
            throw new RuntimeException("OCR识别失败");
        }
        
        return recognizedText;
    }
} 