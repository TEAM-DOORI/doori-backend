package com.doori.doori_backend.post.service;

import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.domain.MemberStatus;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.block.service.BlockService;
import com.doori.doori_backend.global.error.ErrorCode;
import com.doori.doori_backend.global.exception.CustomException;
import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import com.doori.doori_backend.post.dto.request.PostCreateRequest;
import com.doori.doori_backend.post.dto.request.PostUpdateRequest;
import com.doori.doori_backend.post.dto.response.PostListResponse;
import com.doori.doori_backend.post.dto.response.PostListResponse.Wrapper;
import com.doori.doori_backend.post.dto.response.PostResponse;
import com.doori.doori_backend.post.repository.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Post 도메인 비즈니스 로직
 * - 모든 조회는 readOnly=true 트랜잭션 (성능 최적화)
 * - 모든 쓰기는 @Transactional (변경 감지 보장)
 * - PostResponse 변환은 반드시 트랜잭션 내에서 수행 (LazyInitializationException 방지)
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final BlockService blockService;

    /**
     * 게시글 생성
     * @param memberId 인증된 사용자 ID
     * @param request  생성 요청 DTO
     * @return 생성된 게시글 응답
     */
    @Transactional
    public PostResponse createPost(Long memberId, PostCreateRequest request) {
        Member author = findActiveMember(memberId);
        PostType postType = parsePostType(request.postType());

        Post post = Post.builder()
            .author(author)
            .postType(postType)
            .title(request.title())
            .region(request.region())
            .university(request.university())
            .monthlyRent(request.monthlyRent())
            .deposit(request.deposit())
            .description(request.description())
            .roomImages(request.roomImages())
            .build();

        return PostResponse.from(postRepository.save(post));
    }

    /**
     * 게시글 목록 조회 (페이지네이션 + 타입 필터 + 차단 유저 제외)
     * @param memberId    인증된 사용자 ID
     * @param postTypeStr postType 문자열 (null이면 전체)
     * @param page        0-based 페이지 번호
     * @param size        페이지 크기
     * @return 목록 + 전체 개수 래퍼
     */
    @Transactional(readOnly = true)
    public Wrapper getPosts(Long memberId, String postTypeStr, int page, int size) {
        PostType postType = (postTypeStr == null || postTypeStr.isBlank()) ? null : parsePostType(postTypeStr);
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAllByFilterExcludingAuthors(memberId, postType, pageable);

        List<PostListResponse> posts = postPage.getContent()
            .stream()
            .map(PostListResponse::from)
            .toList();

        return Wrapper.of(posts, (int) postPage.getTotalElements());
    }

    /**
     * 게시글 단건 조회 (차단한 유저의 게시글은 404 반환)
     * @param memberId 인증된 사용자 ID
     * @param postId   게시글 ID
     * @return 게시글 상세 응답
     */
    @Transactional(readOnly = true)
    public PostResponse getPost(Long memberId, Long postId) {
        Post post = findPost(postId);
        if (blockService.isBlocked(memberId, post.getAuthor().getId())) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return PostResponse.from(post);
    }

    /**
     * 게시글 수정
     * @param memberId 인증된 사용자 ID
     * @param postId   수정할 게시글 ID
     * @param request  수정 요청 DTO
     */
    @Transactional
    public void updatePost(Long memberId, Long postId, PostUpdateRequest request) {
        Post post = findPost(postId);
        validateOwnership(post, memberId);
        PostType postType = parsePostType(request.postType());

        post.update(
            postType,
            request.title(),
            request.region(),
            request.university(),
            request.monthlyRent(),
            request.deposit(),
            request.description(),
            request.roomImages()
        );
        // 변경 감지(dirty checking)로 별도 save 불필요
    }

    /**
     * 게시글 삭제
     * @param memberId 인증된 사용자 ID
     * @param postId   삭제할 게시글 ID
     */
    @Transactional
    public void deletePost(Long memberId, Long postId) {
        Post post = findPost(postId);
        validateOwnership(post, memberId);
        postRepository.delete(post);
    }

    // ─── 헬퍼 메서드 ────────────────────────────────────────────────────────────

    /**
     * 게시글 존재 확인 (없으면 POST_NOT_FOUND 예외)
     */
    private Post findPost(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    /**
     * 게시글 소유권 검증 (본인이 아니면 POST_FORBIDDEN 예외)
     */
    private void validateOwnership(Post post, Long memberId) {
        if (!post.isOwnedBy(memberId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
    }

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
     * postType 문자열을 PostType Enum으로 변환 (유효하지 않으면 COMMON_BAD_REQUEST 예외)
     */
    private PostType parsePostType(String postTypeStr) {
        try {
            return PostType.valueOf(postTypeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(
                ErrorCode.COMMON_BAD_REQUEST,
                "유효하지 않은 게시글 유형입니다: " + postTypeStr
            );
        }
    }
}
