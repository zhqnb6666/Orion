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

    @Column(nullable = false, length = 100)
    private String projectKey;

    @JsonProperty("isExecutionSuccess")
    @Column(nullable = false)
    private boolean isExecutionSuccess;

    @JsonProperty("isCompiled")
    @Column(nullable = false)
    private boolean isCompiled;
}
