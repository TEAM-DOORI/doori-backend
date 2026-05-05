package com.doori.doori_backend.auth.dto.request;

import com.doori.doori_backend.auth.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank String verificationToken,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String name,
    String nickname,
    @NotNull Gender gender
) {}
