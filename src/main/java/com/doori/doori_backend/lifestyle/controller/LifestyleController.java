package com.doori.doori_backend.lifestyle.controller;

import com.doori.doori_backend.global.response.ApiResponse;
import com.doori.doori_backend.lifestyle.dto.request.LifestyleUpdateRequest;
import com.doori.doori_backend.lifestyle.dto.response.LifestyleProfileResponse;
import com.doori.doori_backend.lifestyle.service.LifestyleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lifestyle")
@RequiredArgsConstructor
public class LifestyleController {

    private final LifestyleService lifestyleService;

    @GetMapping
    public ResponseEntity<ApiResponse<LifestyleProfileResponse>> getMyLifestyle(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(lifestyleService.getMyLifestyle(memberId)));
    }

    @PutMapping("/edit")
    public ResponseEntity<Void> updateLifestyle(
        @RequestBody @Valid LifestyleUpdateRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        lifestyleService.updateLifestyle(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
