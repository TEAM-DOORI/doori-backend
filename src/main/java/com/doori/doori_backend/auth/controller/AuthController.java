package com.doori.doori_backend.auth.controller;

import com.doori.doori_backend.auth.dto.request.EmailSendRequest;
import com.doori.doori_backend.auth.dto.request.EmailVerifyRequest;
import com.doori.doori_backend.auth.dto.request.LoginRequest;
import com.doori.doori_backend.auth.dto.request.SignupRequest;
import com.doori.doori_backend.auth.dto.request.TokenRefreshRequest;
import com.doori.doori_backend.auth.dto.response.EmailVerifyResponse;
import com.doori.doori_backend.auth.dto.response.LoginResponse;
import com.doori.doori_backend.auth.dto.response.SignupResponse;
import com.doori.doori_backend.auth.dto.response.TokenResponse;
import com.doori.doori_backend.auth.service.AuthService;
import com.doori.doori_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerificationCode(
        @RequestBody @Valid EmailSendRequest request
    ) {
        authService.sendEmailVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.success("인증 코드가 발송되었습니다.", null));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyEmailCode(
        @RequestBody @Valid EmailVerifyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyEmailCode(request)));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
        @RequestBody @Valid SignupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
        @RequestBody @Valid TokenRefreshRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        authService.logout(memberId);
        return ResponseEntity.noContent().build();
    }
}
