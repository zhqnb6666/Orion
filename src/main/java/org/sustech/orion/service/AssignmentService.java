package org.sustech.orion.service;

import org.sustech.orion.model.Assignment;

import java.sql.Timestamp;
import java.util.List;

public interface AssignmentService {
    Assignment createAssignment(Assignment assignment, Long courseId);

    List<Assignment> getActiveAssignments(Long courseId);

    List<Assignment> getAssignmentsByType(Long courseId, String type);

    void extendDueDate(Long assignmentId, Timestamp newDueDate);
}
