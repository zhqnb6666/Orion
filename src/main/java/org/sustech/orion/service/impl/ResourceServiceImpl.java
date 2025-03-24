package org.sustech.orion.service.impl;

import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Attachment;
import org.sustech.orion.model.Resource;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sustech.orion.service.AttachmentService;
import org.sustech.orion.service.ResourceService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final AttachmentService attachmentService;

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               CourseRepository courseRepository,
                               AttachmentService attachmentService) {
        this.resourceRepository = resourceRepository;
        this.courseRepository = courseRepository;
        this.attachmentService = attachmentService;
    }

    @Override
    public List<Resource> getCourseResources(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ApiException("Course not found", HttpStatus.NOT_FOUND);
        }
        return resourceRepository.findByCourse_Id(courseId);
    }

    @Override
    public void deleteResource(Long resourceId) {
        Resource resource = getResourceById(resourceId);
        
        // 删除所有关联的附件
        if (resource.getAttachments() != null && !resource.getAttachments().isEmpty()) {
            for (Attachment attachment : resource.getAttachments()) {
                try {
                    attachmentService.deleteAttachment(attachment.getId());
                } catch (Exception e) {
                    // 记录错误但继续执行
                    // TODO: 添加日志记录
                }
            }
        }
        
        // 删除资源
        resourceRepository.delete(resource);
    }

    @Override
    public Resource getResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ApiException("Resource does not exist", HttpStatus.NOT_FOUND));
    }

    
    @Override
    public Resource saveResource(Resource resource) {
        return resourceRepository.save(resource);
    }
}