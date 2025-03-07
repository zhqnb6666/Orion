package org.sustech.orion.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("adminRolesController")
@RequestMapping("/api/admin/roles")
@Tag(name = "Roles API", description = "APIs Roles management")
public class RolesController {
}
