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
    /**
     * Indicates if execution was successful
     */
    private boolean success;

    /**
     * Output produced by the executed code
     */
    private String output;

    /**
     * Error message if execution failed
     */
    private String error;

    /**
     * HTTP status code returned by JDoodle API
     */
    private Integer statusCode;

    /**
     * Execution metrics such as memory usage and CPU time
     */
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

        /**
         * Whether the code was successfully compiled (for compiled languages)
         */
        private boolean compiled;

        /**
         * Compilation status message (for compiled languages)
         */
        private String compilationStatus;
    }
}