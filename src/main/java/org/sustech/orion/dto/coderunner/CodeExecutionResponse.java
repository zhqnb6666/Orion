package org.sustech.orion.dto.coderunner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Response object for code execution results.
 * Provides a structured format for the execution results from JDoodle API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {
    private boolean success;
    private String output;
    private String error;
    private Integer statusCode;
    private ExecutionMetrics metrics;
    /**
     * Nested class to hold execution performance metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionMetrics {
        /**
         * Memory used during execution (in KB)
         */
        private String memory;
        /**
         * CPU time consumed (in seconds)
         */
        private String cpuTime;
        private boolean compiled;
        /**
         * Compilation status message (for compiled languages)
         */
        private String compilationStatus;
    }
}