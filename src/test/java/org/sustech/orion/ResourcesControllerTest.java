package org.sustech.orion;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.Resource;
import org.sustech.orion.repository.ResourceRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class ResourcesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    @WithUserDetails("student")
    void getCourseResources_ShouldReturnResources() throws Exception {
        mockMvc.perform(get("/api/students/resources/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("test resource"));

    }

    @Test
    @WithUserDetails("student")
    void getResourceDetails_WithValidResource_ShouldReturnDetails() throws Exception {
        Resource testResource = resourceRepository.findAll().get(0);

        mockMvc.perform(get("/api/students/resources/" + testResource.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("test resource"))
                .andExpect(jsonPath("$.description").value("test description"));
    }


    @Test
    @WithUserDetails("student")
    void getCourseResources_WhenNotEnrolled_ShouldReturn403() throws Exception {
        // 测试未参加课程的情况（课程2不存在或学生未注册）
        mockMvc.perform(get("/api/students/resources/course/2")).toString().equals("404");

    }
}
