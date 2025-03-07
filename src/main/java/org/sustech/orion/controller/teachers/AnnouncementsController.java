package org.sustech.orion.controller.teachers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("studentsAnnouncementsController")
@RequestMapping("/api/teachers/resources")
@Tag(name = "Announcements API", description = "APIs for announcements management")
public class AnnouncementsController {
}

