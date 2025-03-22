package org.sustech.orion.dto;

import lombok.Data;

@Data
public class TestcaseDTO {
    private String input;
    private String expectedOutput;
    private Double weight;
}