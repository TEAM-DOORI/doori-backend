package com.doori.doori_backend.post;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.doori.doori_backend.user.domain.Gender;
import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.user.repository.MemberRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Post API 통합 테스트
 * - @Transactional: 각 테스트 후 DB 롤백으로 상태 격리
 * - MockMvcBuilders.webAppContextSetup() + springSecurity()로 Security 필터 적용
 *   → SecurityMockMvcRequestPostProcessors.authentication()을 요청별로 주입하여 인증 처리
 * - H2 인메모리 DB (src/test/resources/application.properties 기준)
 */
@SpringBootTest
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private MockMvc mockMvc;
    private Member owner;
    private Member otherMember;
    private Post savedPost;

    @BeforeEach
    void setUp() {
        // springSecurity() 적용으로 SecurityMockMvcRequestPostProcessors 동작 보장
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // 게시글 작성자 회원 생성
        owner = memberRepository.save(Member.builder()
            .email("owner@sju.ac.kr")
            .password("password123!")
            .name("작성자")
            .nickname("ownerNick")
            .gender(Gender.M)
            .school(School.SEJONG_UNIV)
            .build());

        // 타 회원 생성 (권한 없음 테스트용)
        otherMember = memberRepository.save(Member.builder()
            .email("other@sju.ac.kr")
            .password("password123!")
            .name("타회원")
            .nickname("otherNick")
            .gender(Gender.F)
            .school(School.SEJONG_UNIV)
            .build());

        // 테스트용 게시글 사전 저장
        savedPost = postRepository.save(Post.builder()
            .author(owner)
            .postType(PostType.TRANSFER)
            .title("서울대 근처 원룸 양도")
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

    // ─── POST /api/posts ────────────────────────────────────────────────────

    @Test
    void createPost_success() throws Exception {
        mockMvc.perform(post("/api/posts")
                .with(authentication(authOf(owner.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "postType": "TRANSFER",
                        "title": "강남 원룸 양도합니다",
                        "region": "서울 강남구",
                        "university": "연세대학교",
                        "monthlyRent": 700000,
                        "deposit": 5000000,
                        "description": "신축 원룸입니다.",
                        "roomImages": ["https://example.com/room.jpg"]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("게시글이 등록되었습니다."))
            .andExpect(jsonPath("$.data.title").value("강남 원룸 양도합니다"))
            .andExpect(jsonPath("$.data.postType").value("TRANSFER"))
            .andExpect(jsonPath("$.data.authorId").value(owner.getId()));
    }

    @Test
    void createPost_missingField_returns400() throws Exception {
        // title 누락 → @NotBlank 유효성 검증 실패 (C001)
        mockMvc.perform(post("/api/posts")
                .with(authentication(authOf(owner.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "postType": "TRANSFER",
                        "region": "서울 강남구",
                        "university": "연세대학교",
                        "monthlyRent": 700000,
                        "deposit": 5000000,
                        "description": "설명"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C001"));
    }

    // ─── GET /api/posts ──────────────────────────────────────────────────────

    @Test
    void getPosts_noFilter_returnsAll() throws Exception {
        mockMvc.perform(get("/api/posts")
                .with(authentication(authOf(owner.getId())))
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.posts[0].title").value("서울대 근처 원룸 양도"));
    }

    @Test
    void getPosts_withFilter_returnsFiltered() throws Exception {
        // SUBLEASE 타입 게시글 추가
        postRepository.save(Post.builder()
            .author(owner)
            .postType(PostType.SUBLEASE)
            .title("단기 전대합니다")
            .region("서울 강북구")
            .university("서울대학교")
            .monthlyRent(400000)
            .deposit(1000000)
            .description("2개월 단기 전대")
            .roomImages(Collections.emptyList())
            .build());

        mockMvc.perform(get("/api/posts")
                .with(authentication(authOf(owner.getId())))
                .param("postType", "SUBLEASE")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.posts[0].postType").value("SUBLEASE"));
    }

    @Test
    void getPosts_invalidType_returns400() throws Exception {
        // 존재하지 않는 postType → parsePostType에서 COMMON_BAD_REQUEST(C001) 발생
        mockMvc.perform(get("/api/posts")
                .with(authentication(authOf(owner.getId())))
                .param("postType", "INVALID_TYPE"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C001"));
    }

    // ─── GET /api/posts/{postId} ─────────────────────────────────────────────

    @Test
    void getPost_exists_returns200() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", savedPost.getPostId())
                .with(authentication(authOf(owner.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.postId").value(savedPost.getPostId()))
            .andExpect(jsonPath("$.data.title").value("서울대 근처 원룸 양도"))
            .andExpect(jsonPath("$.data.authorNickname").value("ownerNick"));
    }

    @Test
    void getPost_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", 999999L)
                .with(authentication(authOf(owner.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("P001"));
    }

    // ─── PUT /api/posts/{postId} ─────────────────────────────────────────────

    @Test
    void updatePost_owner_returns204() throws Exception {
        mockMvc.perform(put("/api/posts/{postId}", savedPost.getPostId())
                .with(authentication(authOf(owner.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "postType": "SUBLEASE",
                        "title": "수정된 제목",
                        "region": "서울 마포구",
                        "university": "홍익대학교",
                        "monthlyRent": 600000,
                        "deposit": 2000000,
                        "description": "수정된 설명",
                        "roomImages": []
                    }
                    """))
            .andExpect(status().isNoContent());
    }

    @Test
    void updatePost_notOwner_returns403() throws Exception {
        // 타 회원으로 요청 → POST_FORBIDDEN(P002)
        mockMvc.perform(put("/api/posts/{postId}", savedPost.getPostId())
                .with(authentication(authOf(otherMember.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "postType": "TRANSFER",
                        "title": "무단 수정 시도",
                        "region": "서울",
                        "university": "서울대학교",
                        "monthlyRent": 500000,
                        "deposit": 1000000,
                        "description": "설명",
                        "roomImages": []
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("P002"));
    }

    @Test
    void updatePost_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/posts/{postId}", 999999L)
                .with(authentication(authOf(owner.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "postType": "TRANSFER",
                        "title": "없는 게시글 수정",
                        "region": "서울",
                        "university": "서울대학교",
                        "monthlyRent": 500000,
                        "deposit": 1000000,
                        "description": "설명",
                        "roomImages": []
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("P001"));
    }

    // ─── DELETE /api/posts/{postId} ──────────────────────────────────────────

    @Test
    void deletePost_owner_returns204() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", savedPost.getPostId())
                .with(authentication(authOf(owner.getId()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void deletePost_notOwner_returns403() throws Exception {
        // 타 회원으로 요청 → POST_FORBIDDEN(P002)
        mockMvc.perform(delete("/api/posts/{postId}", savedPost.getPostId())
                .with(authentication(authOf(otherMember.getId()))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("P002"));
    }

    @Test
    void deletePost_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", 999999L)
                .with(authentication(authOf(owner.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("P001"));
    }
}
