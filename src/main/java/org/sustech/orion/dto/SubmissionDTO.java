package org.sustech.orion.dto;

import lombok.Getter;
import org.sustech.orion.model.SubmissionContent;

import java.util.List;

@Getter
public class SubmissionDTO {
    private String newStatus;
    private List<SubmissionContent> contents;
}
