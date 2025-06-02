package org.sustech.orion;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.config.SecurityConfig;
import org.sustech.orion.controller.students.AssignmentController;
import org.sustech.orion.model.*;
import org.sustech.orion.repository.*;

import java.sql.Timestamp;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@SpringBootTest
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubmissionConfigRepository submissionConfigRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    // 测试获取作业详情
    @Test
    @WithUserDetails("student")
    void getAssignmentDetails_WhenAuthorized_ShouldReturnDetails() throws Exception {
        // 验证测试数据存在性
        Assignment assignment = assignmentRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("测试作业未初始化"));

        mockMvc.perform(get("/api/students/assignments/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(assignment.getTitle()));
    }


    // 测试剩余提交次数
    @Test
    @WithUserDetails("student")
    void getRemainingAttempts_WhenMaxAttemptsExhausted_ShouldReturnZero() throws Exception {
        // 初始化提交配置
        SubmissionConfig config = submissionConfigRepository.findByAssignmentId(1L);
        config.setMaxSubmissionAttempts(3);
        submissionConfigRepository.save(config);

        // 模拟3次提交（需要根据实际业务逻辑实现）
        simulateSubmissions(1L, 3);

        mockMvc.perform(get("/api/students/assignments/1/submissions/remaining"))
                .andExpect(content().string("0"));
    }

    private void simulateSubmissions(Long assignmentId, int count) {
        User student = userRepository.findByUsername("student");


        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("无效作业ID"));

        for (int i = 0; i < count; i++) {
            Submission submission = new Submission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setSubmitTime(Timestamp.from(Instant.now()));
            submission.setStatus(Submission.SubmissionStatus.PENDING); // 设置状态字段
            submissionRepository.save(submission);
        }
    }

}

