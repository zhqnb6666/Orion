package org.sustech.orion.dto;

import lombok.Data;
import java.util.List;

@Data
public class CodeSubmissionResult {
    private Long submissionId;
    private String script;
    private String language;
    private Integer versionIndex;
    private Double score;
    private List<TestCaseResult> testCaseResults;
    private String status;
    private String feedback;

    @Data
    public static class TestCaseResult {
        private Long testCaseId;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String error;
        private Integer statusCode;
        private String memory;
        private String cpuTime;
        private String compilationStatus;
        private boolean executionSuccess;
        private boolean compiled;
        private boolean passed;
        private Double weight;
    }
} 