package org.sustech.orion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sustech.orion.dto.coderunner.CodeExecutionRequest;
import org.sustech.orion.dto.coderunner.CodeExecutionResponse;
import org.sustech.orion.model.CodeExecutionResult;
import org.sustech.orion.util.CodeRunnerUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/code")
@Tag(name = "Code Execution API", description = "APIs for executing code in various programming languages")
public class CodeRunnerController {

    private final CodeRunnerUtil codeRunnerUtil;
    private final ObjectMapper objectMapper;

    public CodeRunnerController(
            @Value("${jdoodle.clientId}") String clientId,
            @Value("${jdoodle.clientSecret}") String clientSecret,
            ObjectMapper objectMapper) {
        this.codeRunnerUtil = new CodeRunnerUtil(clientId, clientSecret);
        this.objectMapper = objectMapper;
    }

    @PostMapping("/execute")
    @Operation(summary = "Execute code", description = "Executes code in the specified programming language and returns the execution result")
    public ResponseEntity<CodeExecutionResponse> executeCode(@RequestBody CodeExecutionRequest request) {
        try {
            String rawResponse = codeRunnerUtil.executeCode(
                    request.getScript(),
                    request.getLanguage(),
                    request.getVersionIndex(),
                    request.getStdin()
            );

            CodeExecutionResult result = objectMapper.readValue(rawResponse, CodeExecutionResult.class);

            CodeExecutionResponse response = CodeExecutionResponse.builder()
                    .success(result.isExecutionSuccess())
                    .output(result.getOutput())
                    .error(result.getError())
                    .statusCode(result.getStatusCode())
                    .metrics(CodeExecutionResponse.ExecutionMetrics.builder()
                            .memory(result.getMemory())
                            .cpuTime(result.getCpuTime())
                            .compiled(result.isCompiled())
                            .compilationStatus(result.getCompilationStatus())
                            .build())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    CodeExecutionResponse.builder()
                            .success(false)
                            .error("Failed to execute code: " + e.getMessage())
                            .build()
            );
        }
    }
}
