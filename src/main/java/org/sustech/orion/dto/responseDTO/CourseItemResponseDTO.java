package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
// 对应Course模型
public class CourseItemResponseDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String semester;
    private String description;
    private Boolean isActive;
    private Timestamp createdAt;
}
