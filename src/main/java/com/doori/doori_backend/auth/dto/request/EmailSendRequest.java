package com.doori.doori_backend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendRequest(
    @NotBlank @Email String email
) {}
