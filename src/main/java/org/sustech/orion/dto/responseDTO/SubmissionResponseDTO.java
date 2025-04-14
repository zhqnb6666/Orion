package org.sustech.orion.dto.responseDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.sustech.orion.model.*;
import org.sustech.orion.model.Submission.SubmissionStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SubmissionResponseDTO {
    private Long id;

    @JsonIgnore
    private User student;

    @JsonIgnore
    private Assignment assignment;

    private Timestamp submitTime;

    private SubmissionStatus status;

    private Double score; // 新增成绩字段


    private List<SubmissionContent> contents;


    @JsonIgnore
    private List<CodeExecutionResult> codeExecutionResults;

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", submitTime=" + submitTime +
                ", status=" + status +
                '}';
    }

}