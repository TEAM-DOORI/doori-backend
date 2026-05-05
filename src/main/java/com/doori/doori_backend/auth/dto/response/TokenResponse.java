package com.doori.doori_backend.auth.dto.response;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {}
