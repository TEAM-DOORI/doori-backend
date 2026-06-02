package com.doori.doori_backend.chat.repository;

import com.doori.doori_backend.chat.domain.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
}
