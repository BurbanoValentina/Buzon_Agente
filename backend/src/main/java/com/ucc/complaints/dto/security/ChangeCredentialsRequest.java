package com.ucc.complaints.dto.security;

import jakarta.validation.constraints.NotBlank;

public record ChangeCredentialsRequest(
    @NotBlank String username,
    @NotBlank String currentPassword,
    @NotBlank String newPassword,
    @NotBlank String securityAnswer,
    String otpCode
) {
    public ChangeCredentialsRequest(String username, String currentPassword, String newPassword, String securityAnswer) {
        this(username, currentPassword, newPassword, securityAnswer, null);
    }
}
