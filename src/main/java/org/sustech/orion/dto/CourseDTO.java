package org.sustech.orion.dto;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class CourseDTO {
    private String courseName;
    private String courseCode;
    private String description="";
    private Boolean isActive=true;
}
