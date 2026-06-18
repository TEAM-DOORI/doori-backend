package com.doori.doori_backend.chat.repository;

import com.doori.doori_backend.chat.domain.ChatRoom;
import com.doori.doori_backend.chat.domain.RoomType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT r FROM ChatRoom r
        WHERE r.id IN (
            SELECT m.roomId FROM ChatRoomMember m WHERE m.memberId = :memberId
        )
        ORDER BY r.updatedAt DESC
        """)
    List<ChatRoom> findAllByMemberId(@Param("memberId") Long memberId);

    // DM 채팅방 중복 생성 방지 — 두 멤버가 공유하는 DM 방 조회
    @Query("""
        SELECT r FROM ChatRoom r
        WHERE r.type = :type
          AND r.id IN (SELECT m.roomId FROM ChatRoomMember m WHERE m.memberId = :memberId1)
          AND r.id IN (SELECT m.roomId FROM ChatRoomMember m WHERE m.memberId = :memberId2)
        """)
    Optional<ChatRoom> findDmRoom(
        @Param("type") RoomType type,
        @Param("memberId1") Long memberId1,
        @Param("memberId2") Long memberId2
    );
}
