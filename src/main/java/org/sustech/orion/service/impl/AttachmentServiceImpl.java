package org.sustech.orion.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.repository.AttachmentRepository;
import org.sustech.orion.service.AttachmentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    
    @Value("${file.upload.dir:uploads/attachments}")
    private String uploadDir;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public Attachment uploadAttachment(MultipartFile file, Attachment.AttachmentType attachmentType) {
        if (file.isEmpty()) {
            throw new ApiException("文件为空", HttpStatus.BAD_REQUEST);
        }
        
        try {
            // 创建上传目录
            String dirPath = uploadDir + "/" + attachmentType.name().toLowerCase();
            Path directory = Paths.get(dirPath);
            Files.createDirectories(directory);
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename : "unnamed");
            Path filePath = directory.resolve(filename);
            
            // 保存文件
            Files.write(filePath, file.getBytes());
            
            // 创建附件记录
            Attachment attachment = new Attachment();
            attachment.setName(originalFilename != null ? originalFilename : "unnamed");
            attachment.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            attachment.setSize(file.getSize());
            attachment.setUrl(dirPath + "/" + filename);
            attachment.setUploadedAt(Timestamp.from(Instant.now()));
            attachment.setAttachmentType(attachmentType);
            
            // 保存附件记录
            return attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new ApiException("文件上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Attachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ApiException("附件不存在", HttpStatus.NOT_FOUND));
    }

    @Override
    public byte[] downloadAttachment(Long id) throws IOException {
        Attachment attachment = getAttachmentById(id);
        Path filePath = Paths.get(attachment.getUrl());
        
        if (!Files.exists(filePath)) {
            throw new ApiException("文件不存在于服务器", HttpStatus.NOT_FOUND);
        }
        
        return Files.readAllBytes(filePath);
    }

    @Override
    public void deleteAttachment(Long id) {
        Attachment attachment = getAttachmentById(id);
        
        // 删除物理文件
        try {
            Path filePath = Paths.get(attachment.getUrl());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new ApiException("文件删除失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // 删除数据库记录
        attachmentRepository.deleteById(id);
    }

    @Override
    public List<Attachment> getAttachmentsByType(Attachment.AttachmentType attachmentType) {
        return attachmentRepository.findByAttachmentType(attachmentType);
    }
} 