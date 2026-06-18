package com.doori.doori_backend.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record RoomEnterRequest(@NotNull Long roomId) {}
