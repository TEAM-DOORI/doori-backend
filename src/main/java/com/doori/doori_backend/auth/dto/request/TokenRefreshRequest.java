package com.doori.doori_backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank String refreshToken
) {}
