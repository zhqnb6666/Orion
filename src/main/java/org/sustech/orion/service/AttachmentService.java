package org.sustech.orion.service;

import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.model.Attachment;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {
    /**
     * 上传附件文件
     * @param file 上传的文件
     * @param attachmentType 附件类型 (Resource或Submission)
     * @return 创建的附件对象
     */
    Attachment uploadAttachment(MultipartFile file, Attachment.AttachmentType attachmentType);
    
    /**
     * 根据ID获取附件
     * @param id 附件ID
     * @return 附件对象
     */
    Attachment getAttachmentById(Long id);
    
    /**
     * 下载附件文件
     * @param id 附件ID
     * @return 文件字节数组
     * @throws IOException 如果文件无法读取
     */
    byte[] downloadAttachment(Long id) throws IOException;
    
    /**
     * 删除附件
     * @param id 附件ID
     */
    void deleteAttachment(Long id);
    
    /**
     * 获取指定类型的所有附件
     * @param attachmentType 附件类型
     * @return 附件列表
     */
    List<Attachment> getAttachmentsByType(Attachment.AttachmentType attachmentType);
} 