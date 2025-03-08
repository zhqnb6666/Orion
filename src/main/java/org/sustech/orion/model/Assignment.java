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

    @JsonIgnore
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases;

    @Column(nullable = false)
    private Boolean isVisible;
}