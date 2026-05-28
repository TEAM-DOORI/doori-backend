package com.doori.doori_backend.post.controller;

import com.doori.doori_backend.global.response.ApiResponse;
import com.doori.doori_backend.post.dto.request.PostCreateRequest;
import com.doori.doori_backend.post.dto.request.PostUpdateRequest;
import com.doori.doori_backend.post.dto.response.PostListResponse.Wrapper;
import com.doori.doori_backend.post.dto.response.PostResponse;
import com.doori.doori_backend.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

/**
 * Post API 컨트롤러
 * - 모든 엔드포인트는 JWT 인증 필요 (SecurityConfig에서 anyRequest().authenticated() 적용)
 * - Authentication.getPrincipal()로 memberId(Long) 추출
 * - 예외 처리는 GlobalExceptionHandler에 위임, Controller는 try-catch 없음
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 생성
     * POST /api/posts → 201 Created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
        @RequestBody @Valid PostCreateRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        PostResponse response = postService.createPost(memberId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("게시글이 등록되었습니다.", response));
    }

    /**
     * 게시글 목록 조회 (필터 + 페이지네이션 + 차단 유저 제외)
     * GET /api/posts?postType=TRANSFER&page=0&size=10 → 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Wrapper>> getPosts(
        Authentication authentication,
        @RequestParam(required = false) String postType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        Wrapper response = postService.getPosts(memberId, postType, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 단건 조회 (차단한 유저의 게시글은 404 반환)
     * GET /api/posts/{postId} → 200 OK
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        PostResponse response = postService.getPost(memberId, postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 수정
     * PUT /api/posts/{postId} → 204 No Content
     */
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
        @PathVariable Long postId,
        @RequestBody @Valid PostUpdateRequest request,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        postService.updatePost(memberId, postId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 게시글 삭제
     * DELETE /api/posts/{postId} → 204 No Content
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        postService.deletePost(memberId, postId);
        return ResponseEntity.noContent().build();
    }
}
