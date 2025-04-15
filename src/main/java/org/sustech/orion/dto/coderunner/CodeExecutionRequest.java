package org.sustech.orion.dto.coderunner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionRequest {
    private String script;
    private String language;
    private String versionIndex;
    private String stdin;
}
