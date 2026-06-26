package com.doori.doori_backend.chat.repository;

import com.doori.doori_backend.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 커서 기반 페이징 (무한 스크롤용 — offset 방식보다 성능 우수)
    Slice<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(
        Long roomId, Long cursorId, Pageable pageable);
}
