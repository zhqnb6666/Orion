package org.sustech.orion.dto;

import lombok.Getter;
import java.sql.Timestamp;

@Getter
public class AssignmentDTO {
    private String title;
    private String description;
    private String type;
    private Timestamp dueDate;
    private Boolean isVisible=true;
    private Integer maxScore=100;

}
