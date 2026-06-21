package com.doori.doori_backend.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record RoomLeaveRequest(@NotNull Long roomId) {}
