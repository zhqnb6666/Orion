package org.sustech.orion.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sustech.orion.dto.ProfileUpdateDTO;
import org.sustech.orion.model.User;
import org.sustech.orion.service.UserService;
import org.sustech.orion.service.AttachmentService;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile API", description = "APIs for user profile management")
public class ProfileController {

    private final UserService userService;
    private final AttachmentService attachmentService;

    public ProfileController(UserService userService, AttachmentService attachmentService) {
        this.userService = userService;
        this.attachmentService = attachmentService;
    }

    @GetMapping
    @Operation(summary = "获取个人资料",
            description = "获取当前登录用户的个人资料信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved profile")
            })
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserById(user.getUserId()));
    }

    @PutMapping
    @Operation(summary = "更新个人资料",
            description = "更新当前登录用户的个人资料信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
            })
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ProfileUpdateDTO profileUpdateDTO) {
        return ResponseEntity.ok(userService.updateProfile(user.getUserId(), profileUpdateDTO));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像",
            description = "上传或更新用户头像",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully")
            })
    public ResponseEntity<User> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.updateAvatar(user.getUserId(), file));
    }
} 