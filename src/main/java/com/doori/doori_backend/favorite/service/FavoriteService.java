package com.doori.doori_backend.favorite.service;

import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.user.domain.MemberStatus;
import com.doori.doori_backend.user.repository.MemberRepository;
import com.doori.doori_backend.favorite.domain.PostFavorite;
import com.doori.doori_backend.favorite.domain.UserFavorite;
import com.doori.doori_backend.favorite.dto.response.FavoritePostResponse;
import com.doori.doori_backend.favorite.dto.response.FavoriteUserResponse;
import com.doori.doori_backend.favorite.repository.PostFavoriteRepository;
import com.doori.doori_backend.favorite.repository.UserFavoriteRepository;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.repository.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜(Favorite) 도메인 비즈니스 로직
 * - 모든 조회는 readOnly=true 트랜잭션 (성능 최적화)
 * - 모든 쓰기는 @Transactional (변경 감지 보장)
 * - ApiResponse, ErrorResponse 반환 금지 — 예외는 CustomException으로만 처리
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final PostFavoriteRepository postFavoriteRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    // ─── 유저 찜 ─────────────────────────────────────────────────────────────

    /**
     * 유저 찜 추가
     * @param memberId 인증된 사용자 ID (찜하는 주체)
     * @param targetId 찜 대상 사용자 ID
     */
    @Transactional
    public void addUserFavorite(Long memberId, Long targetId) {
        // 자기 자신 찜 불가
        if (memberId.equals(targetId)) {
            throw new CustomException(ErrorCode.COMMON_BAD_REQUEST, "자기 자신을 찜할 수 없습니다.");
        }

        // 중복 찜 방지 (DB 조회 전에 먼저 확인)
        if (userFavoriteRepository.existsByMemberIdAndTargetId(memberId, targetId)) {
            throw new CustomException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        Member member = findActiveMember(memberId);
        Member target = findActiveMember(targetId);

        userFavoriteRepository.save(UserFavorite.builder()
            .member(member)
            .target(target)
            .build());
    }

    /**
     * 유저 찜 삭제
     * @param memberId 인증된 사용자 ID
     * @param targetId 찜 대상 사용자 ID
     */
    @Transactional
    public void removeUserFavorite(Long memberId, Long targetId) {
        UserFavorite userFavorite = userFavoriteRepository
            .findByMemberIdAndTargetId(memberId, targetId)
            .orElseThrow(() -> new CustomException(ErrorCode.FAVORITE_NOT_FOUND));

        userFavoriteRepository.delete(userFavorite);
    }

    /**
     * 내 찜 유저 목록 조회
     * @param memberId 인증된 사용자 ID
     * @return 찜한 유저 응답 목록
     */
    @Transactional(readOnly = true)
    public List<FavoriteUserResponse> getFavoriteUsers(Long memberId) {
        return userFavoriteRepository.findAllByMemberIdWithTarget(memberId)
            .stream()
            .map(uf -> FavoriteUserResponse.from(uf.getTarget()))
            .toList();
    }

    // ─── 게시글 찜 ───────────────────────────────────────────────────────────

    /**
     * 게시글 찜 추가
     * @param memberId 인증된 사용자 ID
     * @param postId   찜 대상 게시글 ID
     */
    @Transactional
    public void addPostFavorite(Long memberId, Long postId) {
        // 중복 찜 방지 (DB 조회 전에 먼저 확인)
        if (postFavoriteRepository.existsByMemberAndPost(memberId, postId)) {
            throw new CustomException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        Member member = findActiveMember(memberId);
        Post post = findPost(postId);

        postFavoriteRepository.save(PostFavorite.builder()
            .member(member)
            .post(post)
            .build());
    }

    /**
     * 게시글 찜 삭제
     * @param memberId 인증된 사용자 ID
     * @param postId   찜 대상 게시글 ID
     */
    @Transactional
    public void removePostFavorite(Long memberId, Long postId) {
        PostFavorite postFavorite = postFavoriteRepository
            .findByMemberAndPost(memberId, postId)
            .orElseThrow(() -> new CustomException(ErrorCode.FAVORITE_NOT_FOUND));

        postFavoriteRepository.delete(postFavorite);
    }

    /**
     * 내 찜 게시글 목록 조회
     * - post와 post.author를 fetch join하여 N+1 방지
     * - roomImages(@ElementCollection) 접근은 트랜잭션 내에서 수행
     * @param memberId 인증된 사용자 ID
     * @return 찜한 게시글 응답 목록
     */
    @Transactional(readOnly = true)
    public List<FavoritePostResponse> getFavoritePosts(Long memberId) {
        return postFavoriteRepository.findAllByMemberIdWithPost(memberId)
            .stream()
            .map(pf -> FavoritePostResponse.from(pf.getPost()))
            .toList();
    }

    // ─── 헬퍼 메서드 ─────────────────────────────────────────────────────────

    /**
     * ACTIVE 상태 회원 조회 (없거나 비활성이면 USER_NOT_FOUND 예외)
     */
    private Member findActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return member;
    }

    /**
     * 게시글 존재 확인 (없으면 POST_NOT_FOUND 예외)
     */
    private Post findPost(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
