package com.doori.doori_backend.block;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doori.doori_backend.user.domain.Gender;
import com.doori.doori_backend.user.domain.Member;
import com.doori.doori_backend.user.repository.MemberRepository;
import com.doori.doori_backend.block.domain.Block;
import com.doori.doori_backend.block.repository.BlockRepository;
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
 * 유저 차단 API 통합 테스트
 * - @Transactional: 각 테스트 후 DB 롤백으로 상태 격리
 * - MockMvcBuilders.webAppContextSetup() + springSecurity()로 Security 필터 적용
 * - H2 인메모리 DB (src/test/resources/application.properties 기준)
 */
@SpringBootTest
@Transactional
class BlockApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BlockRepository blockRepository;

    private MockMvc mockMvc;

    private Member me;
    private Member targetUser;
    private Post targetPost;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        me = memberRepository.save(Member.builder()
            .email("me@sju.ac.kr")
            .password("password123!")
            .name("나")
            .nickname("meNick")
            .gender(Gender.M)
            .school(School.SEJONG_UNIV)
            .build());

        targetUser = memberRepository.save(Member.builder()
            .email("target@sju.ac.kr")
            .password("password123!")
            .name("대상")
            .nickname("targetNick")
            .gender(Gender.F)
            .school(School.SEJONG_UNIV)
            .build());

        targetPost = postRepository.save(Post.builder()
            .author(targetUser)
            .postType(PostType.TRANSFER)
            .title("차단 유저 게시글")
            .region("서울 관악구")
            .university("서울대학교")
            .monthlyRent(500000)
            .deposit(3000000)
            .description("차단 테스트용 게시글입니다.")
            .roomImages(List.of("https://example.com/img1.jpg"))
            .build());
    }

    private Authentication authOf(Long memberId) {
        return new UsernamePasswordAuthenticationToken(memberId, null, Collections.emptyList());
    }

    // ─── 차단 등록 ────────────────────────────────────────────────────────────

    @Test
    void blockUser_success_returns201() throws Exception {
        mockMvc.perform(post("/api/blocks/{targetUserId}", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isCreated());
    }

    @Test
    void blockUser_selfBlock_returns400() throws Exception {
        mockMvc.perform(post("/api/blocks/{targetUserId}", me.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("B001"));
    }

    @Test
    void blockUser_duplicate_returns409() throws Exception {
        blockRepository.save(Block.builder().member(me).target(targetUser).build());

        mockMvc.perform(post("/api/blocks/{targetUserId}", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("B002"));
    }

    @Test
    void blockUser_targetNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/blocks/{targetUserId}", 999999L)
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("U001"));
    }

    // ─── 차단 해제 ────────────────────────────────────────────────────────────

    @Test
    void unblockUser_success_returns204() throws Exception {
        blockRepository.save(Block.builder().member(me).target(targetUser).build());

        mockMvc.perform(delete("/api/blocks/{targetUserId}", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void unblockUser_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/blocks/{targetUserId}", targetUser.getId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("B003"));
    }

    // ─── 차단 목록 조회 ───────────────────────────────────────────────────────

    @Test
    void getBlockedUsers_success() throws Exception {
        blockRepository.save(Block.builder().member(me).target(targetUser).build());

        mockMvc.perform(get("/api/blocks")
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].userId").value(targetUser.getId()))
            .andExpect(jsonPath("$.data[0].nickname").value("targetNick"));
    }

    @Test
    void getBlockedUsers_empty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/blocks")
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    // ─── 차단 유저 게시글 필터링 ────────────────────────────────────────────────

    @Test
    void getPosts_excludesBlockedUserPosts() throws Exception {
        blockRepository.save(Block.builder().member(me).target(targetUser).build());

        mockMvc.perform(get("/api/posts")
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.posts").isArray())
            .andExpect(jsonPath("$.data.posts[?(@.title == '차단 유저 게시글')]").doesNotExist());
    }

    @Test
    void getPost_blockedUserPost_returns404() throws Exception {
        blockRepository.save(Block.builder().member(me).target(targetUser).build());

        mockMvc.perform(get("/api/posts/{postId}", targetPost.getPostId())
                .with(authentication(authOf(me.getId()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("P001"));
    }
}
