package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false, unique = true)
    private String courseCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String semester;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "course_students",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> students;

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments;

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources;

    @Column(nullable = false)
    private Timestamp createdTime;

    @Column(nullable = false)
    private Boolean isActive;

    @Override
    public String toString() {
        return "Course{id=" + id + ", courseName=" + courseName + "'}";
    }
}