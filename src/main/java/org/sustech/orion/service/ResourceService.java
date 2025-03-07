package org.sustech.orion.service;

import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.Resource;

import java.util.List;

public interface ResourceService {
    List<Resource> getCourseResources(Long courseId);

    void deleteResource(Long resourceId);

    String uploadFile(MultipartFile file);

    List<Resource> getAssignmentResources(Long assignmentId);

    Resource getResourceById(Long resourceId);
}
