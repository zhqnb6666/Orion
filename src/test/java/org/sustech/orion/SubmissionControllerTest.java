package org.sustech.orion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.Submission;
import org.sustech.orion.repository.SubmissionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubmissionRepository submissionRepository;

    // 测试获取提交详情
    @Test
    @WithUserDetails("student")
    void getSubmission_ShouldReturnInitializedData() throws Exception {
        // 获取第一个测试提交
        Submission submission = submissionRepository.findAll().get(0);

        mockMvc.perform(get("/api/students/submissions/{id}", submission.getId()))
                .andExpect(status().isOk());

    }



    // 测试无权删除其他用户的提交
    @Test
    @WithUserDetails("teacher")
    void deleteOthersSubmission_ShouldForbid() throws Exception {
        Submission studentSubmission = submissionRepository.findAll().get(0);

        mockMvc.perform(delete("/api/students/submissions/{id}", studentSubmission.getId())).toString().equals("405");

    }

    // 测试获取成绩详情
    @Test
    @WithUserDetails("student")
    void getSubmissionGrade_ShouldReturnGradingInfo() throws Exception {


        mockMvc.perform(get("/api/students/submissions/{id}/grade", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85.0))
                .andExpect(jsonPath("$.feedback").value("Good attempt"));
    }
}
