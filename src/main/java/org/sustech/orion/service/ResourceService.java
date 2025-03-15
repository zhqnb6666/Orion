package org.sustech.orion.service;

import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.Resource;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    List<Resource> getCourseResources(Long courseId);

    void deleteResource(Long resourceId);

    String uploadFile(MultipartFile file);

    Resource getResourceById(Long resourceId);

    byte[] downloadResourceFile(String url) throws IOException;
}
