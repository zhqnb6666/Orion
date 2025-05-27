package org.sustech.orion.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.sustech.orion.service.UserService;

@RestController("adminUserController")
@RequestMapping("/api/admin/users")
@Tag(name = "User API", description = "APIs for user management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    /* useful */

    /* useless */
}
