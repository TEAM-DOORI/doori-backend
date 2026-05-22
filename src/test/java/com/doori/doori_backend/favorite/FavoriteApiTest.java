package com.doori.doori_backend.favorite;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doori.doori_backend.auth.domain.Gender;
import com.doori.doori_backend.auth.domain.Member;
import com.doori.doori_backend.auth.repository.MemberRepository;
import com.doori.doori_backend.favorite.domain.PostFavorite;
import com.doori.doori_backend.favorite.domain.UserFavorite;
import com.doori.doori_backend.favorite.repository.PostFavoriteRepository;
import com.doori.doori_backend.favorite.repository.UserFavoriteRepository;
import com.doori.doori_backend.post.domain.Post;
import com.doori.doori_backend.post.domain.PostType;
import com.doori.doori_backend.post.repository.PostRepository;
import com.doori.doori_backend.school.domain.School;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * 찜(Favorite) API 통합 테스트
 * - @Transactional: 각 테스트 후 DB 롤백으로 상태 격리
 * - MockMvcBuilders.webAppContextSetup() + springSecurity()로 Security 필터 적용
 *   → SecurityMockMvcRequestPostProcessors.authentication()을 요청별로 주입하여 인증 처리
 * - H2 인메모리 DB (src/test/resources/application.properties 기준)
 */
@SpringBootTest
@Transactional
class FavoriteApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserFavoriteRepository userFavoriteRepository;

    @Autowired
    private PostFavoriteRepository postFavoriteRepository;

    private MockMvc mockMvc;

    private Member me;
    private Member targetUser;
    private Post savedPost;

    @BeforeEach
    void setUp() {
        // springSecurity() 적용으로 SecurityMockMvcRequestPostProcessors 동작 보장
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // 요청 주체 회원 생성
        me = memberRepository.save(Member.builder()
            .email("me@sju.ac.kr")
            .password("password123!")
            .name("나")
            .nickname("meNick")
            .gender(Gender.M)
            .school(School.SEJONG_UNIV)
            .build());

        // 찜 대상 회원 생성
        targetUser = memberRepository.save(Member.builder()
            .email("target@sju.ac.kr")
            .password("password123!")
            .name("대상")
            .nickname("targetNick")
            .gender(Gender.F)
            .school(School.SEJONG_UNIV)
            .build());

        // 찜 대상 게시글 생성
        savedPost = postRepository.save(Post.builder()
            .author(targetUser)
            .postType(PostType.TRANSFER)
            .title("원룸 양도합니다")
            .region("서울 관악구")
            .university("서울대학교")
            .monthlyRent(500000)
            .deposit(3000000)
            .description("깨끗한 원룸입니다.")
            .roomImages(List.of("https://example.com/img1.jpg"))
            .build());
    }

    // ─── 인증 헬퍼 ────────────────────────────────────────────────────────────

    /**
     * SecurityMockMvcRequestPostProcessors.authentication()으로 요청에 인증 객체를 주입한다.
     * springSecurity() 설정이 적용된 MockMvc에서만 동작한다.
     */
    private Authentication authOf(Long memberId) {
        return new UsernamePasswordAuthenticationToken(memberId, null, Collections.emptyList());
    }

    // ─── 유저 찜 추가 ────────────────────────────────────────────────────────

    @Test
    void addUserFavorite_success() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/favorites", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void addUserFavorite_duplicate_returns409() throws Exception {
        // 이미 찜된 상태 사전 세팅
        userFavoriteRepository.save(UserFavorite.builder()
            .member(me)
            .target(targetUser)
            .build());

        mockMvc.perform(post("/api/users/{userId}/favorites", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("F001"));
    }

    @Test
    void addUserFavorite_selfFavorite_returns400() throws Exception {
        // 자기 자신 찜 시도 → C001
        mockMvc.perform(post("/api/users/{userId}/favorites", me.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void addUserFavorite_targetNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/favorites", 999999L)
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("U001"));
    }

    // ─── 유저 찜 삭제 ────────────────────────────────────────────────────────

    @Test
    void removeUserFavorite_success() throws Exception {
        // 사전에 찜 등록
        userFavoriteRepository.save(UserFavorite.builder()
            .member(me)
            .target(targetUser)
            .build());

        mockMvc.perform(delete("/api/users/{userId}/favorites", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void removeUserFavorite_notFound_returns404() throws Exception {
        // 찜하지 않은 대상 삭제 시도 → F002
        mockMvc.perform(delete("/api/users/{userId}/favorites", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("F002"));
    }

    // ─── 내 찜 유저 목록 조회 ─────────────────────────────────────────────────

    @Test
    void getFavoriteUsers_success() throws Exception {
        // 사전에 찜 등록
        userFavoriteRepository.save(UserFavorite.builder()
            .member(me)
            .target(targetUser)
            .build());

        mockMvc.perform(get("/api/users/favorites")
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].userId").value(targetUser.getId()))
            .andExpect(jsonPath("$.data[0].nickname").value("targetNick"));
    }

    @Test
    void getFavoriteUsers_empty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/users/favorites")
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    // ─── 게시글 찜 추가 ──────────────────────────────────────────────────────

    @Test
    void addPostFavorite_success() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/favorites", savedPost.getPostId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void addPostFavorite_duplicate_returns409() throws Exception {
        // 이미 찜된 상태 사전 세팅
        postFavoriteRepository.save(PostFavorite.builder()
            .member(me)
            .post(savedPost)
            .build());

        mockMvc.perform(post("/api/posts/{postId}/favorites", savedPost.getPostId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("F001"));
    }

    @Test
    void addPostFavorite_postNotFound_returns404() throws Exception {
        // 존재하지 않는 게시글 찜 → P001
        mockMvc.perform(post("/api/posts/{postId}/favorites", 999999L)
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("P001"));
    }

    // ─── 게시글 찜 삭제 ──────────────────────────────────────────────────────

    @Test
    void removePostFavorite_success() throws Exception {
        // 사전에 찜 등록
        postFavoriteRepository.save(PostFavorite.builder()
            .member(me)
            .post(savedPost)
            .build());

        mockMvc.perform(delete("/api/posts/{postId}/favorites", savedPost.getPostId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void removePostFavorite_notFound_returns404() throws Exception {
        // 찜하지 않은 게시글 삭제 시도 → F002
        mockMvc.perform(delete("/api/posts/{postId}/favorites", savedPost.getPostId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("F002"));
    }

    // ─── 내 찜 게시글 목록 조회 ───────────────────────────────────────────────

    @Test
    void getFavoritePosts_success() throws Exception {
        // 사전에 찜 등록
        postFavoriteRepository.save(PostFavorite.builder()
            .member(me)
            .post(savedPost)
            .build());

        mockMvc.perform(get("/api/posts/favorites")
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].postId").value(savedPost.getPostId()))
            .andExpect(jsonPath("$.data[0].title").value("원룸 양도합니다"))
            .andExpect(jsonPath("$.data[0].postType").value("TRANSFER"));
    }
}
