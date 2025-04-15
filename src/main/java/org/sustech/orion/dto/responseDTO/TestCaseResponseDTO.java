package org.sustech.orion.dto.responseDTO;

import lombok.Getter;
import org.sustech.orion.model.TestCase;

import java.util.List;

@Getter
public class TestCaseResponseDTO {
    private final Long id;
    private final String input;
    private final String expectedOutput;
    private final Integer weight;
    private final Long assignmentId;

    public TestCaseResponseDTO(TestCase testCase) {
        this.id = testCase.getId();
        this.input = testCase.getInput();
        this.expectedOutput = testCase.getExpectedOutput();
        this.weight = testCase.getWeight();
        this.assignmentId = testCase.getAssignment().getId();
    }

    public List<TestCaseResponseDTO> fromTestCases(List<TestCase> testCases) {
        return testCases.stream()
                .map(TestCaseResponseDTO::new)
                .toList();
    }
}
