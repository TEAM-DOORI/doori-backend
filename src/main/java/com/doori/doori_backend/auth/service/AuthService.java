package com.doori.doori_backend.auth.service;

import com.doori.doori_backend.auth.domain.School;
import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.domain.RefreshToken;
import com.doori.doori_backend.auth.dto.request.EmailSendRequest;
import com.doori.doori_backend.auth.dto.request.EmailVerifyRequest;
import com.doori.doori_backend.auth.dto.request.LoginRequest;
import com.doori.doori_backend.auth.dto.request.SignupRequest;
import com.doori.doori_backend.auth.dto.request.TokenRefreshRequest;
import com.doori.doori_backend.auth.dto.response.EmailVerifyResponse;
import com.doori.doori_backend.auth.dto.response.LoginResponse;
import com.doori.doori_backend.auth.dto.response.SignupResponse;
import com.doori.doori_backend.auth.dto.response.TokenResponse;
import com.doori.doori_backend.auth.dto.response.UserInfo;
import com.doori.doori_backend.auth.jwt.JwtProvider;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.auth.repository.RefreshTokenRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationStore verificationStore;
    private final EmailSender emailSender;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void sendEmailVerificationCode(EmailSendRequest request) {
        School.fromEmail(request.email());
        String code = generateCode();
        emailSender.sendVerificationCode(request.email(), code);
        verificationStore.saveCode(request.email(), code);
    }

    public EmailVerifyResponse verifyEmailCode(EmailVerifyRequest request) {
        verificationStore.verifyCode(request.email(), request.code());
        return new EmailVerifyResponse(jwtProvider.createVerificationToken(request.email()));
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = jwtProvider.getEmailFromVerificationToken(request.verificationToken());
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        School school = School.fromEmail(email);
        Member member = memberRepository.save(Member.builder()
            .email(email)
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .nickname(request.nickname())
            .gender(request.gender())
            .school(school)
            .build());
        return SignupResponse.from(member);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        IssuedTokens tokens = issueTokens(member.getId());
        return new LoginResponse(tokens.accessToken(), tokens.refreshToken(), UserInfo.from(member));
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        String token = request.refreshToken();
        Long memberId = jwtProvider.validateAndGetMemberId(token);
        refreshTokenRepository.findById(token)
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_TOKEN));
        IssuedTokens tokens = issueTokens(memberId);
        return new TokenResponse(tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private IssuedTokens issueTokens(Long memberId) {
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        refreshTokenRepository.deleteByMemberId(memberId);
        refreshTokenRepository.save(RefreshToken.builder()
            .token(refreshToken)
            .memberId(memberId)
            .expiresAt(jwtProvider.getRefreshTokenExpiry(refreshToken))
            .build());
        return new IssuedTokens(accessToken, refreshToken);
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }

    private record IssuedTokens(String accessToken, String refreshToken) {}
}
