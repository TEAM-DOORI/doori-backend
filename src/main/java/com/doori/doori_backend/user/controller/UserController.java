package com.doori.doori_backend.user.controller;

import com.doori.doori_backend.global.response.ApiResponse;
import com.doori.doori_backend.user.dto.request.DeleteAccountRequest;
import com.doori.doori_backend.user.dto.request.UpdateProfileRequest;
import com.doori.doori_backend.user.dto.response.ExploreResponse;
import com.doori.doori_backend.user.dto.response.MyProfileResponse;
import com.doori.doori_backend.user.dto.response.ProfileCardResponse;
import com.doori.doori_backend.user.dto.response.RecommendationsResponse;
import com.doori.doori_backend.user.dto.response.UserProfileResponse;
import com.doori.doori_backend.user.service.UserDiscoveryService;
import com.doori.doori_backend.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserDiscoveryService userDiscoveryService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getMyProfile(memberId)));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(
        @RequestBody @Valid UpdateProfileRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        userProfileService.updateProfile(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        Long requesterId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userDiscoveryService.getUserProfile(requesterId, userId)));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<RecommendationsResponse>> getRecommendations(
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userDiscoveryService.getRecommendations(memberId)));
    }

    @GetMapping("/explore")
    public ResponseEntity<ApiResponse<ExploreResponse>> explore(
        @RequestParam(required = false) String residenceType,
        @RequestParam(required = false) Boolean isSmoker,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
            ApiResponse.success(userDiscoveryService.explore(memberId, residenceType, isSmoker, cursor, limit))
        );
    }

    @GetMapping("/{userId}/card")
    public ResponseEntity<ApiResponse<ProfileCardResponse>> getProfileCard(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        Long requesterId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userDiscoveryService.getProfileCard(requesterId, userId)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
        @RequestBody @Valid DeleteAccountRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        userProfileService.deleteAccount(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
