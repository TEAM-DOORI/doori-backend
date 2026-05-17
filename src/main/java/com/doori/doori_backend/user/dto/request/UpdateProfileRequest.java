package com.doori.doori_backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank String name,
    String nickname
) {}
