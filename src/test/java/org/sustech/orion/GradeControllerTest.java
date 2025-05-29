package org.sustech.orion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Grade;
import org.sustech.orion.model.Submission;
import org.sustech.orion.repository.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
