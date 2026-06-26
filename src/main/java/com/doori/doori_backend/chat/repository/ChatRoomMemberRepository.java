package com.doori.doori_backend.chat.repository;

import com.doori.doori_backend.chat.domain.ChatRoomMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    Optional<ChatRoomMember> findByRoomIdAndMemberId(Long roomId, Long memberId);

    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);

    // 방에 한 명이라도 남아 있는지 확인 (leaveRoom 후 구독 해제 여부 판단)
    boolean existsByRoomId(Long roomId);

    List<ChatRoomMember> findByRoomId(Long roomId);

    @Query("SELECT m.memberId FROM ChatRoomMember m WHERE m.roomId = :roomId")
    List<Long> findMemberIdsByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT m FROM ChatRoomMember m WHERE m.roomId IN :roomIds")
    List<ChatRoomMember> findAllByRoomIdIn(@Param("roomIds") Collection<Long> roomIds);

    // 앱 재시작 시 구독 복구용 — 현재 존재하는 모든 roomId 조회
    @Query("SELECT DISTINCT m.roomId FROM ChatRoomMember m")
    List<Long> findAllDistinctRoomIds();
}
