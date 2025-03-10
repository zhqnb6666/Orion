package org.sustech.orion.util;

import org.sustech.orion.dto.responseDTO.*;
import org.sustech.orion.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConvertDTO {
    public static AssignmentResponseDTO toAssignmentResponseDTO(Assignment assignment) {
        AssignmentResponseDTO dto = new AssignmentResponseDTO();

        dto.setId(String.valueOf(assignment.getId()));
        dto.setTitle(assignment.getTitle());
        dto.setType(assignment.getType());
        dto.setDescription(assignment.getDescription());
        dto.setAttachments(toAttachmentResponseDTOList(assignment.getAttachments()));
        dto.setDueDate(assignment.getDueDate());
        dto.setMaxScore(assignment.getMaxScore());
        dto.setStatus(assignment.getStatus().toString());
        dto.setInstructions(assignment.getInstructions());
        dto.setSubmissionUrl(assignment.getSubmissionUrl());

        return dto;
    }

    public static AttachmentResponseDTO toAttachmentResponseDTO(Attachment attachment) {
        AttachmentResponseDTO dto = new AttachmentResponseDTO();

        dto.setId(attachment.getId());
        dto.setName(attachment.getName());
        dto.setSize(String.valueOf(attachment.getSize()));

        return dto;
    }
    // Course模型 转 CourseItemResponseDTO
    public static CourseItemResponseDTO toCourseItemResponseDTO(Course course) {
        CourseItemResponseDTO dto = new CourseItemResponseDTO();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setSemester(course.getSemester());
        dto.setDescription(course.getDescription());
        dto.setIsActive(course.getIsActive());
        dto.setCreatedAt(course.getCreatedTime());
        return dto;
    }

    // Resource模型 转 CourseMaterialResponseDTO
    public static CourseMaterialResponseDTO ResourceToCourseMaterialResponseDTO(Resource resource) {
        CourseMaterialResponseDTO dto = new CourseMaterialResponseDTO();
        dto.setId(resource.getId().toString());
        dto.setTitle(resource.getName());
        dto.setType(resource.getType());
        dto.setDescription(resource.getDescription());
        dto.setAttachments(toAttachmentResponseDTOList(resource.getAttachments()));
        return dto;
    }
    // Assignment模型 转 CourseMaterialResponseDTO
    public static CourseMaterialResponseDTO AssignmentToCourseMaterialResponseDTO(Assignment assignment) {
        CourseMaterialResponseDTO dto = new CourseMaterialResponseDTO();
        dto.setId(assignment.getId().toString());
        dto.setTitle(assignment.getTitle());
        dto.setType(assignment.getType());
        dto.setDescription(assignment.getDescription());
        dto.setAttachments(toAttachmentResponseDTOList(assignment.getAttachments()));
        return dto;
    }

    // Grade模型 转 GradeResponseDTO
    public static GradeResponseDTO toGradeResponseDTO(Grade grade) {
        GradeResponseDTO dto = new GradeResponseDTO();
        dto.setId(grade.getId());
        dto.setName(grade.getSubmission().getAssignment().getTitle());
        dto.setType(grade.getSubmission().getAssignment().getType());
        dto.setScore(grade.getScore());
        dto.setTotalPoints(Double.valueOf(grade.getSubmission().getAssignment().getMaxScore()));
        dto.setDueDate(grade.getSubmission().getAssignment().getDueDate());
        dto.setSubmittedDate(grade.getSubmission().getSubmitTime());
        dto.setGradedDate(grade.getGradedTime());
        dto.setFeedback(grade.getFeedback());
        dto.setAppealReason(grade.getAppealReason());
        dto.setAppealTime(grade.getAppealTime());
        dto.setStatus(grade.getStatus().name());


        return dto;
    }
    // Course模型 转 CourseBasicInfoResponseDTO
    public static CourseBasicInfoResponseDTO toCourseBasicInfoResponseDTO(Course course) {
        CourseBasicInfoResponseDTO dto = new CourseBasicInfoResponseDTO();
        dto.setCourseName(course.getCourseName());
        dto.setTeacher(course.getInstructor().getUsername());
        dto.setEmail(course.getInstructor().getEmail());
        dto.setCourseDescription(course.getDescription());
        return dto;
    }

    // 集合转换方法
    public static List<CourseBasicInfoResponseDTO> toCourseBasicInfoDTOList(List<Course> courses) {
        return toDTOList(courses, ConvertDTO::toCourseBasicInfoResponseDTO);
    }




    // 通用集合转换方法
    public static List<CourseItemResponseDTO> toCourseItemDTOList(List<Course> courses) {
        return toDTOList(courses, ConvertDTO::toCourseItemResponseDTO);
    }

    public static List<CourseMaterialResponseDTO> ResourceToCourseMaterialDTOList(List<Resource> resources) {
        return toDTOList(resources, ConvertDTO::ResourceToCourseMaterialResponseDTO);
    }
    public static List<CourseMaterialResponseDTO> AssignmentToCourseMaterialDTOList(List<Assignment> assignments) {
        return toDTOList(assignments, ConvertDTO::AssignmentToCourseMaterialResponseDTO);
    }

    public static List<GradeResponseDTO> toGradeResponseDTOList(List<Grade> grades) {
        return toDTOList(grades, ConvertDTO::toGradeResponseDTO);
    }




    public static <T, R> List<R> toDTOList(List<T> entities, Function<T, R> converter) {
        return entities.stream()
                .map(converter)
                .collect(Collectors.toList());
    }
    public static List<AssignmentResponseDTO> toAssignmentResponseDTOList(List<Assignment> assignments) {
        return toDTOList(assignments, ConvertDTO::toAssignmentResponseDTO);
    }
    public static List<AttachmentResponseDTO> toAttachmentResponseDTOList(List<Attachment> attachments) {
        return toDTOList(attachments, ConvertDTO::toAttachmentResponseDTO);
    }

}
