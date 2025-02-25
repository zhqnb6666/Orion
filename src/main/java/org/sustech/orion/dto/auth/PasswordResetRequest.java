package org.sustech.orion.dto.auth;

import lombok.Getter;

@Getter
public class PasswordResetRequest {
    private String email;
    private String verificationCode;
    private String newPassword;
}
