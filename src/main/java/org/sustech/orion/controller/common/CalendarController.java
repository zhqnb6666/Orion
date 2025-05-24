package org.sustech.orion.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.dto.CalendarDTO;
import org.sustech.orion.model.User;
import org.sustech.orion.service.CalendarService;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Calendar API", description = "APIs for calendar management")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    @Operation(summary = "获取用户的日历事件",
            description = "获取当前用户的所有日历事件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取日历事件")
            })
    public ResponseEntity<List<CalendarDTO>> getUserEvents(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(calendarService.getUserCalendarEvents(currentUser.getUserId()));
    }

    @PostMapping("/events/custom")
    @Operation(summary = "创建自定义事件",
            description = "为当前用户创建一个自定义日历事件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功创建事件"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效")
            })
    public ResponseEntity<CalendarDTO> createCustomEvent(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Timestamp deadline) {
        return ResponseEntity.ok(
                convertToDTO(calendarService.createCustomEvent(
                        currentUser.getUserId(), title, description, deadline))
        );
    }

    @DeleteMapping("/events/{eventId}")
    @Operation(summary = "删除日历事件",
            description = "删除指定的日历事件",
            responses = {
                    @ApiResponse(responseCode = "204", description = "成功删除事件"),
                    @ApiResponse(responseCode = "404", description = "事件不存在")
            })
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        calendarService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    private CalendarDTO convertToDTO(org.sustech.orion.model.Calendar calendar) {
        CalendarDTO dto = new CalendarDTO();
        dto.setId(calendar.getId());
        dto.setTitle(calendar.getTitle());
        dto.setDeadline(calendar.getDeadline());
        if (calendar.getCourse() != null) {
            dto.setCourseName(calendar.getCourse().getCourseName());
            dto.setCourseId(calendar.getCourse().getId());
        }
        dto.setAssignmentId(calendar.getAssignmentId());
        dto.setDescription(calendar.getDescription());
        dto.setEventType(calendar.getEventType().name());
        return dto;
    }
} 