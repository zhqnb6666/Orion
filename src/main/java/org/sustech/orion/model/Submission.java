package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Timestamp submitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @JsonIgnore
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionContent> contents = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Grade grade;

    @JsonIgnore
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private AIGrading aiGrading;

    @JsonIgnore
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeExecutionResult> codeExecutionResults;

    @Column(nullable = false)
    private Integer attempts;

    @Getter
    public enum SubmissionStatus {
        PENDING, ACCEPTED, REJECTED, DRAFT;
        private final String value;

        SubmissionStatus() {
            this.value = this.name().toLowerCase();
        }

    }


}