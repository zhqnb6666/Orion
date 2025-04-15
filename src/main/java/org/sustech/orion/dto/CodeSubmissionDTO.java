package org.sustech.orion.dto;

import lombok.Data;

@Data
public class CodeSubmissionDTO {
    private String script;
    private String language;
    private Integer versionIndex;
}
