package com.doori.doori_backend.school.dto.response;

import com.doori.doori_backend.school.domain.School;

public record SchoolResponse(String name, String domain) {

    public static SchoolResponse from(School school) {
        return new SchoolResponse(school.getDisplayName(), school.getDomain());
    }
}
