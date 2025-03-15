package org.sustech.orion.service.impl;

import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Resource;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               CourseRepository courseRepository) {
        this.resourceRepository = resourceRepository;
        this.courseRepository = courseRepository;
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
        resourceRepository.deleteById(resourceId);
    }


    @Override
    public String uploadFile(MultipartFile file) {
        // 实现文件存储逻辑，返回文件访问路径
        // 示例：存储到本地文件系统
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get("uploads/" + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new ApiException("文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @Override
    public Resource getResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ApiException("资源不存在", HttpStatus.NOT_FOUND));
    }
    @Override
    public byte[] downloadResourceFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new ApiException("File not found", HttpStatus.NOT_FOUND);
        }
        return Files.readAllBytes(path);
    }
}