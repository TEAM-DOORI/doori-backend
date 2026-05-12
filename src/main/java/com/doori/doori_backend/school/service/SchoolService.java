package com.doori.doori_backend.school.service;

import com.doori.doori_backend.school.domain.School;
import com.doori.doori_backend.school.dto.response.SchoolResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SchoolService {

    public List<SchoolResponse> searchSchools(String keyword) {
        return Arrays.stream(School.values())
            .filter(s -> s.getDisplayName().contains(keyword))
            .map(SchoolResponse::from)
            .toList();
    }
}
