package org.sustech.orion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.sustech.orion.dto.ProfileUpdateDTO;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.util.JwtUtil;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // 测试获取个人资料
    @Test
    @WithUserDetails("student")
    void getProfile_ShouldReturnUserInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    // 测试更新基础信息
    @Test
    @WithUserDetails("student")
    void updateProfile_WithValidData_ShouldUpdateSuccessfully() throws Exception {
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setEmail("new_student@example.com");
        updateDTO.setBio("Updated bio information");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new_student@example.com"))
                .andExpect(jsonPath("$.bio").value("Updated bio information"));
    }

    // 测试密码修改
    @Test
    @WithUserDetails("student")
    void updateProfile_WithPasswordChange_ShouldUpdatePassword() throws Exception {
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setOldPassword("student"); // 初始密码为用户名
        updateDTO.setNewPassword("newPassword123");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(updateDTO)))
                .andExpect(status().isOk());

    }


    // 测试邮箱冲突
    @Test
    @WithUserDetails("student")
    void updateProfile_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setEmail("teacher@example.com"); // 已存在的邮箱

        mockMvc.perform(MockMvcRequestBuilders.put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(updateDTO)))
                .andExpect(status().isConflict());
    }

    // 测试无效密码修改
    @Test
    @WithUserDetails("student")
    void updateProfile_WithInvalidOldPassword_ShouldReturnBadRequest() throws Exception {
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setOldPassword("wrongPassword");
        updateDTO.setNewPassword("newPassword123");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(updateDTO)))
                .andExpect(status().isBadRequest());
    }
    // 辅助工具类
    class TestUtils {
        public static String asJsonString(final Object obj) {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}




