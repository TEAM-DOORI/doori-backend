package com.doori.doori_backend.auth.domain;

import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import java.util.Arrays;

public enum School {
    SEJONG_UNIV("sju.ac.kr", "세종대학교"),
    KONKUK_UNIV("konkuk.ac.kr", "건국대학교"),
    HANYANG_UNIV("hanyang.ac.kr","한양대학교");



    private final String domain;
    private final String displayName;

    School(String domain, String displayName) {
        this.domain = domain;
        this.displayName = displayName;
    }

    public String getDomain() {
        return domain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static School fromEmail(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        return Arrays.stream(values())
            .filter(s -> s.domain.equals(domain))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_UNSUPPORTED_EMAIL));
    }
}
