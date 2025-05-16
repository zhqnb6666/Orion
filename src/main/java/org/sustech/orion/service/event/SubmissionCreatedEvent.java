package org.sustech.orion.service.event;

import org.springframework.context.ApplicationEvent;

/**
 * 提交创建事件，用于触发自动AI评分
 */
public class SubmissionCreatedEvent extends ApplicationEvent {
    
    private final Long submissionId;
    
    public SubmissionCreatedEvent(Object source, Long submissionId) {
        super(source);
        this.submissionId = submissionId;
    }
    
    public Long getSubmissionId() {
        return submissionId;
    }
} 