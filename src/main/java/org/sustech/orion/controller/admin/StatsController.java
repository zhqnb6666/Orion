package org.sustech.orion.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("adminStatsController")
@RequestMapping("/api/admin/stats")
@Tag(name = "Stats API", description = "APIs Stats management")
public class StatsController {
}
