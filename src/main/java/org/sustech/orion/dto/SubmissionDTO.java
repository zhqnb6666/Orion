package org.sustech.orion.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.SubmissionContent;

import java.util.List;

@Getter @Setter
public class SubmissionDTO {
    private List<SubmissionContentDTO> contents;
    private List<MultipartFile> files; // 新增文件字段
    private String newStatus;

    @Getter @Setter
    public static class SubmissionContentDTO {
        private String type;
        private String content;
        private String fileUrl;
    }
}
