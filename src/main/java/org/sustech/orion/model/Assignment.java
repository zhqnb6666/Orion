package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Timestamp dueDate;

    @Column(nullable = false)
    private Integer maxScore;

    @Column(columnDefinition = "TEXT")
    private String instructions;//new

    @Column(nullable = false)
    private String submissionUrl;//new

    @OneToMany
    @JsonIgnore
    @JoinColumn(name = "assignment_id", nullable = false)
    private List<Attachment> attachments;

    @JsonIgnore
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status{
        OPEN,CLOSED,UPCOMING
    }

}