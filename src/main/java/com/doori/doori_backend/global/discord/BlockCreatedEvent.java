package com.doori.doori_backend.global.discord;

public record BlockCreatedEvent(Long blockerId, String blockerNickname, String targetNickname) {}
