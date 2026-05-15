package com.doori.doori_backend.user.controller;

import com.doori.doori_backend.global.response.ApiResponse;
import com.doori.doori_backend.user.dto.request.DeleteAccountRequest;
import com.doori.doori_backend.user.dto.request.UpdateProfileRequest;
import com.doori.doori_backend.user.dto.response.ExploreResponse;
import com.doori.doori_backend.user.dto.response.MyProfileResponse;
import com.doori.doori_backend.user.dto.response.ProfileImageResponse;
import com.doori.doori_backend.user.dto.response.RecommendationsResponse;
import com.doori.doori_backend.user.dto.response.UserProfileResponse;
import com.doori.doori_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(memberId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
        @RequestBody @Valid UpdateProfileRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        userService.updateProfile(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다.", null));
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        String imageUrl = userService.updateProfileImage(memberId, file);
        return ResponseEntity.ok(ApiResponse.success(new ProfileImageResponse(imageUrl)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        Long requesterId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(requesterId, userId)));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<RecommendationsResponse>> getRecommendations(
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userService.getRecommendations(memberId)));
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
            ApiResponse.success(userService.explore(memberId, residenceType, isSmoker, cursor, limit))
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
        @RequestBody @Valid DeleteAccountRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        userService.deleteAccount(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
