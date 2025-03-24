package org.sustech.orion.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.SubmissionContent;

import java.util.List;

@Getter @Setter
public class SubmissionDTO {
    private String textResponse;
    private List<MultipartFile> files;
}
