package org.sustech.orion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionDTO {
    private Double score;
    private List<TestCaseResult> testCaseResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseResult {
        private Long testCaseId;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String error;
        private boolean passed;
        private Double weight;
        private Integer statusCode;
        private String memory;
        private String cpuTime;
        private boolean compiled;
        private String compilationStatus;
    }
}