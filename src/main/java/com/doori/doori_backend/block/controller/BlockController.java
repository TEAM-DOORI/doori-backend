package com.doori.doori_backend.block.controller;

import com.doori.doori_backend.block.dto.response.BlockedUserResponse;
import com.doori.doori_backend.block.service.BlockService;
import com.doori.doori_backend.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    /**
     * 유저 차단 등록
     * POST /api/blocks/{targetUserId} → 201 Created
     */
    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> blockUser(
        @PathVariable Long targetUserId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        blockService.blockUser(memberId, targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 유저 차단 해제
     * DELETE /api/blocks/{targetUserId} → 204 No Content
     */
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> unblockUser(
        @PathVariable Long targetUserId,
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        blockService.unblockUser(memberId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 차단 유저 목록 조회
     * GET /api/blocks → 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlockedUserResponse>>> getBlockedUsers(
        Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        List<BlockedUserResponse> response = blockService.getBlockedUsers(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
