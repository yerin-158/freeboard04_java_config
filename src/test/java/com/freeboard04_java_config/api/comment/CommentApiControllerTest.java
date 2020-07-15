package com.freeboard04_java_config.api.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeboard04_java_config.api.user.UserForm;
import com.freeboard04_java_config.config.ApplicationConfig;
import com.freeboard04_java_config.config.WebConfig;
import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.board.BoardRepository;
import com.freeboard04_java_config.domain.comment.CommentEntity;
import com.freeboard04_java_config.domain.comment.CommentRepository;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import com.freeboard04_java_config.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.persistence.EntityManager;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationConfig.class, WebConfig.class})
@Transactional
@WebAppConfiguration
class CommentApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mvc;

    @Autowired
    private MockHttpSession mockHttpSession;

    private UserEntity testUser;
    private BoardEntity testBoard;

    private ObjectMapper objectMapper;


    @BeforeEach
    public void initMvc() {
        initSessionAndBoard();

        objectMapper = new ObjectMapper();

        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    private void initSessionAndBoard() {
        testUser = userRepository.findAll().get(0);
        UserForm userForm = UserForm.builder().accountId(testUser.getAccountId()).password(testUser.getPassword()).build();
        testBoard = boardRepository.findAllByWriterId(testUser.getId()).get(0);

        mockHttpSession.setAttribute("USER", userForm);
    }

    @Test
    void get_test() throws Exception {
        mvc.perform(get("/api/comments")
                .param("boardId", String.valueOf(testBoard.getId()))
                .session(mockHttpSession))
                .andExpect(status().isOk());
    }

    @Test
    void post_test() throws Exception {
        String TestContents = "댓글 달기 테스트~~ "+TestUtil.getRandomString(20);

        ObjectMapper objectMapper = new ObjectMapper();
        CommentForm commentForm = CommentForm.builder().contents(TestContents).build();

        MvcResult response = mvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(commentForm))
                .param("boardId", String.valueOf(testBoard.getId()))
                .session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        String contents = response.getResponse().getContentAsString();
        CommentDto savedComment = objectMapper.readValue(contents, CommentDto.class);
        assertThat(savedComment.getContents(), equalTo(TestContents));
    }

    @Test
    void update_test() throws Exception {
        CommentEntity savedComment = createPrevEntity();

        String TestContents = "댓글 업데이트 테스트~~ "+TestUtil.getRandomString(20);

        CommentForm commentForm = CommentForm.builder().contents(TestContents).build();

        MvcResult response = mvc.perform(put("/api/comments/"+savedComment.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(commentForm))
                .session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        String contents = response.getResponse().getContentAsString();
        CommentDto updatedComment = objectMapper.readValue(contents, CommentDto.class);
        assertThat(updatedComment.getContents(), equalTo(TestContents));
    }

    private CommentEntity createPrevEntity() {
        CommentForm prevComment = CommentForm.builder().contents("이전 값").build();
        CommentEntity savedComment = commentRepository.save(prevComment.convertCommentEntity(testUser, testBoard));
        entityManager.flush();
        return savedComment;
    }

    @Test
    void delete_test() throws Exception {
        CommentEntity savedComment = createPrevEntity();

        mvc.perform(delete("/api/comments/"+savedComment.getId())
                .session(mockHttpSession))
                .andExpect(status().isOk());

        Optional<CommentEntity> deletedComment = commentRepository.findById(savedComment.getId());

        assertThat(deletedComment.isPresent(), equalTo(false));
    }

}
