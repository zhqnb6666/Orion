package org.sustech.orion.dto;

import lombok.Getter;

@Getter
public class CourseDTO {
    private String courseName;
    private String courseCode;
    private String description="";
    private Boolean isActive=true;
}
