package org.sustech.orion.dto.authDTO;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String usernameOrEmail;
    private String password;
}
