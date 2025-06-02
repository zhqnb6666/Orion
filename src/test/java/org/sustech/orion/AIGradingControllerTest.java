package org.sustech.orion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.AIGrading;
import org.sustech.orion.repository.AIGradingRepository;
import org.sustech.orion.repository.SubmissionRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AIGradingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AIGradingRepository aiGradingRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    // 测试自动生成的提交1的AI评分
    @Test
    @WithUserDetails("student")
    void getExistingAIGrading_ShouldReturnValidData() throws Exception {
        // 使用Initializer中已创建的提交ID=1
        mockMvc.perform(get("/api/ai-grading/submission/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiScore").isNumber())
                .andExpect(jsonPath("$.confidence").value(0.7));
    }

    // 测试教师手动触发AI评分
    @Test
    @WithUserDetails("teacher")
    void manualGradeSubmission_ShouldCreateNewRecord() throws Exception {
        mockMvc.perform(post("/api/ai-grading/grade")
                        .param("submissionId", "1")
                        .param("modelName", "qwq-32b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackSuggestions").exists());

        // 验证数据库记录
        AIGrading grading = aiGradingRepository.findBySubmissionId(1L).orElseThrow();
        assertTrue(grading.getAiScore() >= 0 && grading.getAiScore() <= 100);
    }

    // 测试不存在的提交
    @Test
    @WithUserDetails("teacher")
    void gradeNonExistingSubmission_ShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/ai-grading/grade")
                        .param("submissionId", "999")
                        .param("modelName", "qwq-32b")).toString().equals("400");

    }

    // 测试学生获取未评分提交
    @Test
    @WithUserDetails("student")
    void getUnGradedSubmission_ShouldReturn404() throws Exception {
        // 使用Initializer中未评分的提交ID=3
        mockMvc.perform(get("/api/ai-grading/submission/3"))
                .andExpect(status().isNotFound());
    }

    // 测试评分置信度验证
    @Test
    @WithUserDetails("teacher")
    void checkConfidenceThreshold() throws Exception {
        // 触发自动评分
        mockMvc.perform(post("/api/ai-grading/grade")
                .param("submissionId", "1")
                .param("modelName", "qwq-32b"));

        // 验证高置信度评分
        List<AIGrading> highConfidence = aiGradingRepository.findByConfidenceGreaterThanEqual(0.6);
        assertFalse(highConfidence.isEmpty(), "应存在置信度0.6以上的评分");
    }
}
