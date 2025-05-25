package org.sustech.orion.service;

import org.sustech.orion.dto.CalendarDTO;
import org.sustech.orion.model.Calendar;
import org.sustech.orion.model.User;

import java.util.List;

public interface CalendarService {
    List<CalendarDTO> getUserCalendarEvents(Long userId);
    List<Calendar> getAllCalendarsByAssignmentId(Long assignmentId);
    Calendar createAssignmentEvent(Long courseId, Long assignmentId, String title, User user, String description, java.sql.Timestamp deadline);
    Calendar createCustomEvent(Long userId, String title, String description, java.sql.Timestamp deadline);
    void deleteEvent(Long eventId);
} 