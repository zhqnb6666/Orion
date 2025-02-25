package org.sustech.orion.dto.auth;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String usernameOrEmail;
    private String password;
}
