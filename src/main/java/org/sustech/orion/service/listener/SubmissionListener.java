package org.sustech.orion.service.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.sustech.orion.model.AIGrading;
import org.sustech.orion.model.Submission;
import org.sustech.orion.service.AIGradingService;
import org.sustech.orion.service.event.SubmissionCreatedEvent;

@Component
public class SubmissionListener {

    @Autowired
    private AIGradingService aiGradingService;

    /**
     * 监听提交创建事件，在事务提交后自动进行AI评分
     * 
     * @param event 提交创建事件
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionCreated(SubmissionCreatedEvent event) {
        // 获取提交ID
        Long submissionId = event.getSubmissionId();
        
        System.out.println("收到提交创建事件，准备为提交ID: " + submissionId + " 进行AI评分");
        
        // 调用AI评分服务进行自动评分
        try {
            AIGrading aiGrading = aiGradingService.gradeSubmission(submissionId, "qwq-32b");
            System.out.println("AI评分完成，提交ID: " + submissionId + 
                    "，分数: " + aiGrading.getAiScore() + 
                    "，置信度: " + aiGrading.getConfidence());
        } catch (Exception e) {
            // 记录异常但不阻止正常流程
            System.err.println("自动AI评分失败，提交ID: " + submissionId + "，原因: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 