package org.sustech.orion.service;

import org.sustech.orion.model.Resource;

import java.util.List;

public interface ResourceService {
    List<Resource> getCourseResources(Long courseId);

    void deleteResource(Long resourceId);
}
