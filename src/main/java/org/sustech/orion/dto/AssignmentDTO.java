package org.sustech.orion.dto;

import lombok.Getter;
import java.sql.Timestamp;

@Getter
public class AssignmentDTO {
    private String title;
    private String description;
    private String type;
    private Timestamp dueDate;
    private String status="UPCOMING";
    private Integer maxScore=100;

}
