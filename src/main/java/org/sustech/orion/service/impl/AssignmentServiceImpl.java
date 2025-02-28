package org.sustech.orion.service.impl;

import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Course;
import org.sustech.orion.repository.AssignmentRepository;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 CourseRepository courseRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public Assignment createAssignment(Assignment assignment, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException("Course not found", HttpStatus.NOT_FOUND));
        assignment.setCourse(course);
        return assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> getActiveAssignments(Long courseId) {
        return assignmentRepository.findByCourse_IdAndDueDateAfter(
                courseId, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public List<Assignment> getAssignmentsByType(Long courseId, String type) {
        return assignmentRepository.findByCourse_IdAndType(courseId, type);
    }

    @Override
    public void extendDueDate(Long assignmentId, Timestamp newDueDate) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException("Assignment not found", HttpStatus.NOT_FOUND));
        assignment.setDueDate(newDueDate);
        assignmentRepository.save(assignment);
    }
}