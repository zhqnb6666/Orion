package org.sustech.orion.dto.coderunner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for code execution.
 * Contains all necessary parameters to execute code via the JDoodle API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionRequest {
    /**
     * The source code script to be executed
     */
    private String script;

    /**
     * The programming language (e.g., "java", "python3", "cpp", etc.)
     */
    private String language;

    /**
     * Version of the language to use (provided as JDoodle versionIndex)
     */
    private String versionIndex;

    /**
     * Standard input to provide to the program (optional)
     */
    private String stdin;
}
