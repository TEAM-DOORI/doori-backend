package com.doori.doori_backend.school.controller;

import com.doori.doori_backend.global.response.ApiResponse;
import com.doori.doori_backend.school.dto.response.SchoolResponse;
import com.doori.doori_backend.school.service.SchoolService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> searchSchools(
        @RequestParam(defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(schoolService.searchSchools(keyword)));
    }
}
