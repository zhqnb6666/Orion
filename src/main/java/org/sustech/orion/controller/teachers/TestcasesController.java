package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("teachersTestcasesController")
@RequestMapping("/api/teachers/testcases")
@Tag(name = "Testcases API", description = "APIs for Testcases management")
public class TestcasesController {
}

