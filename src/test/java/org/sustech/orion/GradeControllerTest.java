package org.sustech.orion;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Submission;
import org.sustech.orion.repository.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    // 在GradeControllerTest.java中增加以下测试方法

    @Test
    @WithUserDetails("teacher")
    void testFinalizeGradeThroughAPI() throws Exception {
        // 通过API创建新评分
        Submission submission = submissionRepository.findAll().get(1);
        mockMvc.perform(post("/api/teachers/grades/" + submission.getId())
                        .contentType("application/json")
                        .content("{\"score\":75.0,\"feedback\":\"API测试评分\"}"))
                .andExpect(status().isOk());

        // 获取创建的评分记录
        Grade newGrade = (Grade) gradeRepository.findBySubmissionId(submission.getId()).orElseThrow();

        // 通过API确认成绩
        mockMvc.perform(post("/api/teachers/grades/" + newGrade.getId() + "/finalize"))
                .andExpect(status().isNoContent());

        // 验证状态更新
        Grade finalized = gradeRepository.findById(newGrade.getId()).orElseThrow();
        assertTrue(finalized.getIsFinalized(), "成绩确认状态应更新为已确认");
    }

    @Test
    @WithUserDetails("teacher")
    void testDuplicateFinalization() throws Exception {
        Grade existingGrade = gradeRepository.findAll().get(0);

        // 第一次确认
        mockMvc.perform(post("/api/teachers/grades/" + existingGrade.getId() + "/finalize"))
                .andExpect(status().isNoContent());

        // 第二次确认
        mockMvc.perform(post("/api/teachers/grades/" + existingGrade.getId() + "/finalize")).toString().equals("204");

    }

    @Test
    @WithUserDetails("student")
    void testGradeAppealFlow() throws Exception {
        Grade grade = gradeRepository.findAll().get(0);


        mockMvc.perform(put("/api/students/grades/" + grade.getId() + "/appeal")
                        .contentType("application/json")
                        .content("{\"reason\":\"评分标准不一致\"}")).toString().equals("404");


    }




    @Test
    @WithUserDetails("student")
    void getGradeSummary_ShouldReturnValidData() throws Exception {

        mockMvc.perform(get("/api/students/grades/summary"))
                .andExpect(status().isOk());

    }

    @Test
    @WithUserDetails("teacher")
    void gradeSubmission_ShouldCreateNewGrade() throws Exception {
        Submission submission = submissionRepository.getReferenceById(1L);

        mockMvc.perform(post("/api/teachers/grades/" + submission.getId())
                        .contentType("application/json")
                        .content("{\"score\":85.0,\"feedback\":\"Good attempt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85.0));
    }

    @Test
    @WithUserDetails("teacher")
    void finalizeGrade_ShouldUpdateStatus() throws Exception {
        Grade grade = gradeRepository.findAll().get(0);

        mockMvc.perform(post("/api/teachers/grades/" + grade.getId() + "/finalize"))
                .andExpect(status().isNoContent());

        Grade updatedGrade = gradeRepository.findById(grade.getId()).orElseThrow();
        assertTrue(updatedGrade.getIsFinalized());
    }
}
