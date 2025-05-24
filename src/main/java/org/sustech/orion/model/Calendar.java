package org.sustech.orion.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "calendar_events")
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Timestamp deadline;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = true)
    private Long assignmentId;

    @Column(nullable = true)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    public enum EventType {
        ASSIGNMENT,
        NOTIFICATION,
        CUSTOM
    }
} 