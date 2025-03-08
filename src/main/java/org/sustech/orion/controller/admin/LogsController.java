package org.sustech.orion.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("adminLogsController")
@RequestMapping("/api/admin/logs")
@Tag(name = "Logs API", description = "APIs Logs management")

public class LogsController {
}
