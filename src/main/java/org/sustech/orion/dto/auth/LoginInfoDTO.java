package org.sustech.orion.dto.authDTO;

import lombok.Data;
import lombok.Getter;
import org.sustech.orion.model.User;

import java.sql.Timestamp;

@Data
public class LoginInfoDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String avatarUrl;
    private String bio;
    private boolean isExpired;
}
