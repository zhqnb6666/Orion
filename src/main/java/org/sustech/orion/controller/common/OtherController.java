package org.sustech.orion.controller.common;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("CommonOtherController")
@RequestMapping("/api")
@Tag(name = "Other API", description = "APIs Other management")
public class OtherController {
}
