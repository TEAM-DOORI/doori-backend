package com.doori.doori_backend.auth.dto.response;

public record LoginResponse(String accessToken, String refreshToken, UserInfo user) {}
