package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String url;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private Timestamp uploadTime;

}