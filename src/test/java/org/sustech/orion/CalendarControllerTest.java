package org.sustech.orion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.sustech.orion.model.Calendar;
import org.sustech.orion.repository.CalendarRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalendarRepository calendarRepository;

    // 测试获取日历事件
    @Test
    @WithUserDetails("student")
    void getUserEvents_ShouldReturnCalendarEvents() throws Exception {
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("test open assignment"))
                .andExpect(jsonPath("$[0].eventType").value("ASSIGNMENT"));
    }

    // 测试创建自定义事件
    @Test
    @WithUserDetails("student")
    void createCustomEvent_WithValidParams_ShouldCreateEvent() throws Exception {
        String deadline = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)).toString();

        mockMvc.perform(post("/api/calendar/events/custom")
                        .param("title", "Custom Exam")
                        .param("description", "Final exam preparation")
                        .param("deadline", deadline))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Custom Exam"))
                .andExpect(jsonPath("$.eventType").value("CUSTOM"));
    }

    // 测试删除事件
    @Test
    @WithUserDetails("student")
    void deleteEvent_WithValidEvent_ShouldReturn204() throws Exception {
        // 获取初始化创建的作业事件
        Calendar assignmentEvent = calendarRepository.findAll().stream()
                .filter(e -> e.getEventType() == Calendar.EventType.ASSIGNMENT)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/calendar/events/" + assignmentEvent.getId()))
                .andExpect(status().isNoContent());

        assertFalse(calendarRepository.existsById(assignmentEvent.getId()));
    }

    // 测试非法时间格式
    @Test
    @WithUserDetails("student")
    void createCustomEvent_WithInvalidDateFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/calendar/events/custom")
                        .param("title", "Invalid Event")
                        .param("deadline", "2025-13-01 25:00:00")) // 非法日期格式
                .andExpect(status().isBadRequest());
    }
}
