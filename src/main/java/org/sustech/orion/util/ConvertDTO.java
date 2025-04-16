package org.sustech.orion.util;

import org.sustech.orion.dto.authDTO.LoginInfoDTO;
import org.sustech.orion.dto.responseDTO.*;
import org.sustech.orion.model.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConvertDTO {
    public static AssignmentResponseDTO toAssignmentResponseDTO(Assignment assignment) {
        AssignmentResponseDTO dto = new AssignmentResponseDTO();

        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setType(assignment.getType());
        dto.setDescription(assignment.getDescription());
        dto.setAttachments(toAttachmentResponseDTOList(assignment.getAttachments()));
        dto.setOpenDate(assignment.getOpenDate());
        dto.setDueDate(assignment.getDueDate());
        dto.setMaxScore(assignment.getMaxScore());

        // new
        if (assignment.getOpenDate().after(Timestamp.from(Instant.now()))) {
            dto.setStatus("upcoming");
        } else if (assignment.getDueDate().before(Timestamp.from(Instant.now()))) {
            dto.setStatus("closed");
        } else {
            dto.setStatus("open");
        }

        dto.setInstructions(assignment.getInstructions());
//        dto.setSubmissionUrl(assignment.getSubmissionUrl());

        return dto;
    }

    public static AttachmentResponseDTO toAttachmentResponseDTO(Attachment attachment) {
        AttachmentResponseDTO dto = new AttachmentResponseDTO();

        dto.setId(attachment.getId());
        dto.setName(attachment.getName());
        dto.setSize(FileSizeUtil.formatFileSize(attachment.getSize()));
        dto.setUrl(attachment.getUrl());

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

    // Resource模型 转 ResourceResponseDTO
    public static ResourceResponseDTO toResourceResponseDTO(Resource resource) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setId(resource.getId());
        dto.setTitle(resource.getName());
        dto.setType(resource.getType());
        dto.setDescription(resource.getDescription());
        dto.setUploadTime(resource.getUploadTime());
        dto.setAttachments(toAttachmentResponseDTOList(resource.getAttachments()));
        return dto;
    }

    // Grade模型 转 GradeResponseDTO
    public static GradeResponseDTO toGradeResponseDTO(Grade grade) {
        GradeResponseDTO dto = new GradeResponseDTO();
        dto.setId(grade.getId());
        dto.setTitle(grade.getSubmission().getAssignment().getTitle());
        dto.setType(grade.getSubmission().getAssignment().getType());
        dto.setScore(grade.getScore());
        dto.setMaxScore(Double.valueOf(grade.getSubmission().getAssignment().getMaxScore()));
        dto.setDueDate(grade.getSubmission().getAssignment().getDueDate());
        dto.setSubmittedDate(grade.getSubmission().getSubmitTime());
        dto.setGradedDate(grade.getGradedTime());
        dto.setFeedback(grade.getFeedback());
        dto.setAppealReason(grade.getAppealReason());
        dto.setAppealTime(grade.getAppealTime());
        dto.setStatus(grade.getStatus().getValue());

        // 添加 AI 评分和作业ID
        AIGrading aiGrading = grade.getSubmission().getAiGrading();
        if (aiGrading != null) {
            dto.setAiGrading(aiGrading);
        }
        dto.setAssignmentId(grade.getSubmission().getAssignment().getId());


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
    public static List<CourseBasicInfoResponseDTO> toCourseBasicInfoResponseDTOList(List<Course> courses) {
        return toDTOList(courses, ConvertDTO::toCourseBasicInfoResponseDTO);
    }

    //转loginInfoDTO
    public static LoginInfoDTO toLoginInfoDTO(User user, boolean isExpired) {
        LoginInfoDTO dto = new LoginInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setExpired(isExpired);
        return dto;
    }

    // 新增转换方法
    public static SubmissionResponseDTO toSubmissionResponseDTO(Submission submission) {
        SubmissionResponseDTO dto = new SubmissionResponseDTO();
        dto.setId(submission.getId());
        dto.setStudent(submission.getStudent());
        dto.setStudentName(submission.getStudent().getUsername());
        dto.setAssignment(submission.getAssignment());

        dto.setSubmitTime(submission.getSubmitTime());
        dto.setStatus(submission.getStatus());
        if (submission.getGrade() != null) {
            dto.setGrade(submission.getGrade());
        }
        if(submission.getAiGrading() != null) {
            dto.setAiGrading(submission.getAiGrading());
        }
        dto.setContents(submission.getContents());
        dto.setCodeExecutionResults(submission.getCodeExecutionResults());
        return dto;
    }

    // 通用集合转换方法
    public static List<CourseItemResponseDTO> toCourseItemResponseDTOList(List<Course> courses) {
        return toDTOList(courses, ConvertDTO::toCourseItemResponseDTO);
    }

    public static List<ResourceResponseDTO> toResourceResponseDTOList(List<Resource> resources) {
        return toDTOList(resources, ConvertDTO::toResourceResponseDTO);
    }

    public static List<GradeResponseDTO> toGradeResponseDTOList(List<Grade> grades) {
        return toDTOList(grades, ConvertDTO::toGradeResponseDTO);
    }




    public static <T, R> List<R> toDTOList(List<T> entities, Function<T, R> converter) {
        if (entities == null) {
            return new ArrayList<>();
        }
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
    public static List<SubmissionResponseDTO> toSubmissionResponseDTOList(List<Submission> submissions) {
        return toDTOList(submissions, ConvertDTO::toSubmissionResponseDTO);
    }


}
