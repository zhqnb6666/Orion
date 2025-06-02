package org.sustech.orion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.dto.CourseDTO;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.service.CourseService;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseService courseService;

    // 测试获取用户课程列表
    @Test
    @WithUserDetails("student")
    void getEnrolledCourses_ShouldReturnCourseList() throws Exception {
        // 初始化测试课程
        User teacher = userRepository.findByUsername("teacher");
        Course testCourse = createTestCourse("测试课程", "CS101", teacher);
        courseService.addStudentToCourse(testCourse.getId(),
                userRepository.findByUsername("student"));

        mockMvc.perform(get("/api/students/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].courseCode").value("CS101"));
    }

    // 测试获取课程详情
    @Test
    @WithUserDetails("student")
    void getCourseDetails_WhenAuthorized_ShouldReturnDetails() throws Exception {

        mockMvc.perform(get("/api/students/courses/1"  ))
                .andExpect(status().isOk());

    }

    // 测试添加学生到课程（教师权限）
    @Test
    @WithUserDetails("teacher")
    void addStudentToCourse_WithValidData_ShouldSucceed() throws Exception {
        User teacher = userRepository.findByUsername("teacher");
        Course course = createTestCourse("权限测试课程", "CS103", teacher);


        mockMvc.perform(post("/api/teachers/courses/" + course.getId() + "/students")
                        .param("studentId", "3")).toString().equals("204");


        assertTrue(courseService.isStudentInCourse(course.getId(), Long.valueOf("3")));
    }

    // 测试创建课程
    @Test
    @WithUserDetails("teacher")
    void createCourse_WithAdminRole_ShouldSucceed() throws Exception {
        CourseDTO dto = new CourseDTO();
        dto.setCourseName("新课程");
        dto.setCourseCode("CS104");

        mockMvc.perform(post("/api/teachers/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))).toString().equals("200");


        assertNotNull(courseRepository.findByCourseCode("CS104"));
    }

    private Course createTestCourse(String name, String code, User instructor) {
        Course course = new Course();
        course.setCourseName(name);
        course.setCourseCode(code);
        course.setSemester("2024-Spring");
        course.setInstructor(instructor);
        course.setCreatedTime(Timestamp.from(Instant.now()));
        course.setIsActive(true); // 修复非空字段问题
        return courseRepository.save(course);
    }

    private User createTestUser(String username, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("encoded_password");
        user.setRole(role);
        user.setEmail(username + "@test.com");
        user.setCreatedAt(Timestamp.from(Instant.now()));
        return userRepository.save(user);
    }
}
