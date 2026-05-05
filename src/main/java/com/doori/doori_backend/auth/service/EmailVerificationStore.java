package com.doori.doori_backend.auth.service;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationStore {

    private static final int CODE_VALID_MINUTES = 5;

    private final Map<String, VerificationEntry> codeStore = new ConcurrentHashMap<>();

    public void saveCode(String email, String code) {
        codeStore.put(email, new VerificationEntry(code, LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES)));
    }

    public void verifyCode(String email, String code) {
        VerificationEntry entry = codeStore.get(email);
        if (entry == null) {
            throw new CustomException(ErrorCode.AUTH_INVALID_CODE);
        }
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            codeStore.remove(email);
            throw new CustomException(ErrorCode.AUTH_CODE_EXPIRED);
        }
        if (!entry.code().equals(code)) {
            throw new CustomException(ErrorCode.AUTH_INVALID_CODE);
        }
        codeStore.remove(email);
    }

    private record VerificationEntry(String code, LocalDateTime expiresAt) {}
}
