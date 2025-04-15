package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private Integer weight;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @JsonIgnore
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeExecutionResult> executionResults;
}