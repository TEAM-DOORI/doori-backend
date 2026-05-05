package com.doori.doori_backend.auth.jwt;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String VERIFICATION_TOKEN_TYPE = "verification";

    private final SecretKey secretKey;
    private final long accessTokenValidMs;
    private final long refreshTokenValidMs;
    private final long verificationTokenValidMs;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-valid-ms:3600000}") long accessTokenValidMs,
        @Value("${jwt.refresh-token-valid-ms:2592000000}") long refreshTokenValidMs,
        @Value("${jwt.verification-token-valid-ms:1800000}") long verificationTokenValidMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidMs = accessTokenValidMs;
        this.refreshTokenValidMs = refreshTokenValidMs;
        this.verificationTokenValidMs = verificationTokenValidMs;
    }

    public String createAccessToken(Long memberId) {
        return createToken(String.valueOf(memberId), accessTokenValidMs, ACCESS_TOKEN_TYPE);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(String.valueOf(memberId), refreshTokenValidMs, REFRESH_TOKEN_TYPE);
    }

    public String createVerificationToken(String email) {
        Date now = new Date();
        return Jwts.builder()
            .subject(email)
            .claim(TOKEN_TYPE_CLAIM, VERIFICATION_TOKEN_TYPE)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + verificationTokenValidMs))
            .signWith(secretKey)
            .compact();
    }

    public Long validateAccessTokenAndGetMemberId(String token) {
        return validateTypedTokenAndGetMemberId(token, ACCESS_TOKEN_TYPE);
    }

    public Long validateRefreshTokenAndGetMemberId(String token) {
        return validateTypedTokenAndGetMemberId(token, REFRESH_TOKEN_TYPE);
    }

    public String getEmailFromVerificationToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (!VERIFICATION_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
            }
            return claims.getSubject();
        } catch (CustomException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.AUTH_CODE_EXPIRED);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    public LocalDateTime getRefreshTokenExpiry(String token) {
        Date expiry = getClaims(token).getExpiration();
        return expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Long validateTypedTokenAndGetMemberId(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);
            if (!expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
            }
            return Long.parseLong(claims.getSubject());
        } catch (CustomException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | NumberFormatException e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private String createToken(String subject, long validMs, String type) {
        Date now = new Date();
        return Jwts.builder()
            .subject(subject)
            .claim(TOKEN_TYPE_CLAIM, type)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + validMs))
            .signWith(secretKey)
            .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
