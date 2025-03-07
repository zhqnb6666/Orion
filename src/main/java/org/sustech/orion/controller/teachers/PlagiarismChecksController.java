package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("teachersPlagiarismChecksController")
@RequestMapping("/api/teachers/plagiarismChecks")
@Tag(name = "PlagiarismChecks API", description = "APIs for PlagiarismChecks management")
public class PlagiarismChecksController {
}

