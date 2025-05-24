package org.sustech.orion.service;

import org.sustech.orion.dto.CalendarDTO;
import org.sustech.orion.model.Calendar;

import java.util.List;

public interface CalendarService {
    List<CalendarDTO> getUserCalendarEvents(Long userId);
    Calendar createAssignmentEvent(Long courseId, Long assignmentId, String title, String description, java.sql.Timestamp deadline);
    Calendar createCustomEvent(Long userId, String title, String description, java.sql.Timestamp deadline);
    void deleteEvent(Long eventId);
} 