package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Resource;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sustech.orion.service.ResourceService;

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
}