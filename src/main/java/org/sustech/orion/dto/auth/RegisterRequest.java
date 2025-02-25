package org.sustech.orion.dto.auth;

import lombok.Getter;

@Getter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String verificationCode;
    private String role;
}
