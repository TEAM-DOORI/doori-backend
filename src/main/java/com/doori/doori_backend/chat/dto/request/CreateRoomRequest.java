package com.doori.doori_backend.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateRoomRequest(
    @NotBlank String name,
    @NotEmpty List<Long> memberIds  // 초대할 멤버 ID 목록 (생성자 제외)
) {}
