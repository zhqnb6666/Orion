package org.sustech.orion.service.impl;

import org.springframework.stereotype.Service;
import org.sustech.orion.dto.CalendarDTO;
import org.sustech.orion.exception.ApiException;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Calendar;
import org.sustech.orion.model.Course;
import org.sustech.orion.model.User;
import org.sustech.orion.repository.AssignmentRepository;
import org.sustech.orion.repository.CalendarRepository;
import org.sustech.orion.repository.CourseRepository;
import org.sustech.orion.repository.UserRepository;
import org.sustech.orion.service.CalendarService;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public CalendarServiceImpl(CalendarRepository calendarRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository, AssignmentRepository assignmentRepository) {
        this.calendarRepository = calendarRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<CalendarDTO> getUserCalendarEvents(Long userId) {
        return calendarRepository.findByUser_UserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Calendar> getAllCalendarsByAssignmentId(Long assignmentId) {
        return calendarRepository.findByAssignmentId(assignmentId);
    }

    @Override
    public Calendar createAssignmentEvent(Long courseId, Long assignmentId, String title, User user,
                                        String description, Timestamp deadline) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException("Course not found", HttpStatus.NOT_FOUND));

        Calendar event = new Calendar();
        event.setUser(user);
        event.setTitle(title);
        event.setDescription(description);
        event.setDeadline(deadline);
        event.setCourse(course);
        event.setAssignmentId(assignmentId);
        event.setEventType(Calendar.EventType.ASSIGNMENT);

        return calendarRepository.save(event);
    }

    @Override
    public Calendar createCustomEvent(Long userId, String title, String description, Timestamp deadline) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Calendar event = new Calendar();
        event.setTitle(title);
        event.setDescription(description);
        event.setDeadline(deadline);
        event.setUser(user);
        event.setEventType(Calendar.EventType.CUSTOM);

        return calendarRepository.save(event);
    }

    @Override
    public void deleteEvent(Long eventId) {
        if (!calendarRepository.existsById(eventId)) {
            throw new ApiException("Calendar event not found", HttpStatus.NOT_FOUND);
        }
        calendarRepository.deleteById(eventId);
    }

    private CalendarDTO convertToDTO(Calendar calendar) {
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

        // 新增课程编号处理
        if (calendar.getCourse() != null) {
            dto.setCourseCode(calendar.getCourse().getCourseCode());
        }

        // 新增作业类型处理
        if (calendar.getAssignmentId() != null) {
            Assignment assignment = assignmentRepository.findById(calendar.getAssignmentId())
                    .orElse(null);
            if (assignment != null) {
                dto.setAssignmentType(assignment.getType());
            }
        }
        return dto;
    }
} 