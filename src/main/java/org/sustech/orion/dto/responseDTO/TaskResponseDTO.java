package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

//对应 ASSIGNMENT
@Getter
@Setter
public class TaskResponseDTO {
    private String id;
    private String title;
    private String description;
    private String deadline;
    private boolean completed;
    private String courseId;
    private String courseName;
    private String courseCode;
    private String type;//作业类型


}
