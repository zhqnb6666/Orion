package org.sustech.orion.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String email;
    private String bio;
    private String oldPassword;
    private String newPassword;
} 