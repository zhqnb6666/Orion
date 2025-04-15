package org.sustech.orion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "code_execution_results")
public class CodeExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "testcase_id", nullable = false)
    private TestCase testCase;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String error;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false, length = 50)
    private String memory;

    @Column(nullable = false, length = 50)
    private String cpuTime;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String compilationStatus;

    @JsonProperty("isExecutionSuccess")
    @Column(nullable = false)
    private boolean isExecutionSuccess;

    @JsonProperty("isCompiled")
    @Column(nullable = false)
    private boolean isCompiled;
}
