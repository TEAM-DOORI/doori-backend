package com.doori.doori_backend.favorite.controller;

import com.doori.doori_backend.favorite.dto.response.FavoritePostResponse;
import com.doori.doori_backend.favorite.dto.response.FavoriteUserResponse;
import com.doori.doori_backend.favorite.service.FavoriteService;
import com.doori.doori_backend.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 찜(Favorite) API 컨트롤러
 * - 모든 엔드포인트는 JWT 인증 필요 (SecurityConfig에서 anyRequest().authenticated() 적용)
 * - Authentication.getPrincipal()로 memberId(Long) 추출
 * - 예외 처리는 GlobalExceptionHandler에 위임, Controller는 try-catch 없음
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ─── 유저 찜 엔드포인트 ──────────────────────────────────────────────────

    /**
     * 유저 찜 추가
     * POST /api/users/{userId}/favorites → 204 No Content
     */
    @PostMapping("/users/{userId}/favorites")
    public ResponseEntity<Void> addUserFavorite(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        favoriteService.addUserFavorite(memberId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 유저 찜 삭제
     * DELETE /api/users/{userId}/favorites → 204 No Content
     */
    @DeleteMapping("/users/{userId}/favorites")
    public ResponseEntity<Void> removeUserFavorite(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        favoriteService.removeUserFavorite(memberId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 찜 유저 목록 조회
     * GET /api/users/favorites → 200 OK + List
     */
    @GetMapping("/users/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteUserResponse>>> getFavoriteUsers(
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        List<FavoriteUserResponse> response = favoriteService.getFavoriteUsers(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── 게시글 찜 엔드포인트 ────────────────────────────────────────────────

    /**
     * 게시글 찜 추가
     * POST /api/posts/{postId}/favorites → 204 No Content
     */
    @PostMapping("/posts/{postId}/favorites")
    public ResponseEntity<Void> addPostFavorite(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        favoriteService.addPostFavorite(memberId, postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 게시글 찜 삭제
     * DELETE /api/posts/{postId}/favorites → 204 No Content
     */
    @DeleteMapping("/posts/{postId}/favorites")
    public ResponseEntity<Void> removePostFavorite(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        favoriteService.removePostFavorite(memberId, postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 찜 게시글 목록 조회
     * GET /api/posts/favorites → 200 OK + List
     */
    @GetMapping("/posts/favorites")
    public ResponseEntity<ApiResponse<List<FavoritePostResponse>>> getFavoritePosts(
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        List<FavoritePostResponse> response = favoriteService.getFavoritePosts(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
