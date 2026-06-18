package com.doori.doori_backend.chat.controller;

import com.doori.doori_backend.chat.dto.request.CreateRoomRequest;
import com.doori.doori_backend.chat.dto.response.ChatMessageResponse;
import com.doori.doori_backend.chat.dto.response.ChatRoomResponse;
import com.doori.doori_backend.chat.dto.response.SliceResponse;
import com.doori.doori_backend.chat.service.ChatRoomService;
import com.doori.doori_backend.chat.service.ChatService;
import com.doori.doori_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    // 내가 참여한 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(chatRoomService.getRooms(memberId)));
    }

    // 단체 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
            @RequestBody @Valid CreateRoomRequest request,
            @AuthenticationPrincipal Long memberId) {
        ChatRoomResponse room = chatRoomService.createGroup(request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(room));
    }

    // 1:1 DM 채팅방 조회 또는 생성
    @PostMapping("/rooms/dm/{targetMemberId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getOrCreateDm(
            @PathVariable Long targetMemberId,
            @AuthenticationPrincipal Long memberId) {
        ChatRoomResponse room = chatRoomService.getOrCreateDm(targetMemberId, memberId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    // 메시지 히스토리 (무한 스크롤 — 커서 기반)
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<SliceResponse<ChatMessageResponse>>> getHistory(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "9999999999") Long cursorId,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal Long memberId) {
        SliceResponse<ChatMessageResponse> result =
            chatService.getHistory(roomId, cursorId, size, memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
