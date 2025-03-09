package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 对应Course模型扩展
public class CourseBasicInfoResponseDTO {
    private String courseName;
    private String teacher;
    private String email;
    private String courseDescription;
}
