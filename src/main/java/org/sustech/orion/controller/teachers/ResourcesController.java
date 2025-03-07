package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("teachersResourcesController")
@RequestMapping("/api/teachers/resources")
@Tag(name = "Resources API", description = "APIs for Resources management")
public class ResourcesController {
}

