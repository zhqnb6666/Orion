package org.sustech.orion.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("adminConfigsController")
@RequestMapping("/api/admin/configs")
@Tag(name = "Configs API", description = "APIs Configs management")

public class ConfigsController {
}
