package com.freeboard04_java_config.api.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeboard04_java_config.api.user.UserForm;
import com.freeboard04_java_config.config.ApplicationContext;
import com.freeboard04_java_config.config.WebConfig;
import com.freeboard04_java_config.config.WebInitializer;
import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.board.BoardRepository;
import com.freeboard04_java_config.domain.board.enums.BoardExceptionType;
import com.freeboard04_java_config.domain.board.enums.SearchType;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryEntity;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryRepository;
import com.freeboard04_java_config.domain.goodContentsHistory.enums.GoodContentsHistoryExceptionType;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import com.freeboard04_java_config.util.exception.FreeBoardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationContext.class, WebConfig.class})
@Transactional
@WebAppConfiguration
public class BoardApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoodContentsHistoryRepository goodContentsHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mvc;

    @Autowired
    private MockHttpSession mockHttpSession;

    private UserEntity testUser;
    private BoardEntity testBoard;


    @BeforeEach
    public void initMvc() {
        testUser = userRepository.findAll().get(0);
        UserForm userForm = UserForm.builder().accountId(testUser.getAccountId()).password(testUser.getPassword()).build();
        mockHttpSession.setAttribute("USER", userForm);

        testBoard = boardRepository.findAllByWriterId(testUser.getId()).get(0);

        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("trailing-slash test")
    public void trailingSlashTest() throws Exception {
        mvc.perform(get("/api/boards/")).andExpect(status().isOk());
    }

    @Test
    public void getTest() throws Exception {
        mvc.perform(get("/api/boards")).andExpect(status().isOk());
    }

    @Test
    public void saveTest() throws Exception {
        BoardForm boardForm = BoardForm.builder().title("제목을 입력하세요").contents("내용입니다.").build();
        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(post("/api/boards")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(boardForm)))
                .andExpect(status().isOk())
                .andExpect(content().json("{'contents':'" + boardForm.getContents() + "'}"));
        ;
    }

    @Test
    @DisplayName("올바른 패스워드를 입력한 경우 데이터 수정이 가능하다.")
    public void updateTest() throws Exception {
        BoardForm updateForm = BoardForm.builder().title("제목을 입력하세요").contents("수정된 데이터입니다 ^^*").build();
        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(put("/api/boards/" + testBoard.getId())
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateForm)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("자신이 작성하지 않은 글을 삭제하려고 하면 데이터는 삭제되지 않고 예외처리 된다..")
    public void deleteTest1() throws Exception {
        UserEntity wrongUser = userRepository.findAll().get(1);
        BoardEntity wrongBoard = boardRepository.findAllByWriterId(wrongUser.getId()).get(0);

        mvc.perform(delete("/api/boards/" + wrongBoard.getId())
                .session(mockHttpSession))
                .andExpect(result -> assertEquals(result.getResolvedException().getMessage(), BoardExceptionType.NO_QUALIFICATION_USER.getErrorMessage()))
                .andExpect(result -> assertEquals(result.getResolvedException().getClass().getCanonicalName(), FreeBoardException.class.getCanonicalName()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("자신이 작성한 글을 삭제하려고 하면 데이터를 삭제한다.")
    public void deleteTest2() throws Exception {
        mvc.perform(delete("/api/boards/" + testBoard.getId())
                .session(mockHttpSession))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시판 검색 테스트-타이틀")
    public void searchTest() throws Exception {
        String keyword = "test";
        mvc.perform(get("/api/boards?type=" + SearchType.TITLE + "&keyword=" + keyword)
                .session(mockHttpSession))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시판 검색 테스트-글 작성자")
    public void searchTest2() throws Exception {
        String keyword = "yerin";
        mvc.perform(get("/api/boards?type=" + SearchType.WRITER + "&keyword=" + keyword)
                .session(mockHttpSession))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("아직 좋아요하지 않은 글에 좋아요를 한다.")
    public void good() throws Exception {
        Map<String, Object> target = getTargetUserAndBoard();
        BoardEntity targetBoard = (BoardEntity) target.get("targetBoard");
        setMockHttpSession((UserEntity) target.get("targetUser"));

        assert targetBoard != null;

        mvc.perform(post("/api/boards/" + targetBoard.getId() + "/good")
                .session(mockHttpSession))
                .andExpect(status().isOk());
    }

    private Map<String, Object> getTargetUserAndBoard() {
        Map<String, Object> target = new HashMap<>();
        List<UserEntity> userEntities = userRepository.findAll();

        for (UserEntity user : userEntities) {
            List<BoardEntity> boardEntities = boardRepository.findAll(new Specification<BoardEntity>() {
                @Override
                public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.notEqual(root.get("writer"), user);
                }
            });

            for (BoardEntity board : boardEntities) {
                if (goodContentsHistoryRepository.findAllByUserAndBoard(user, board).size() == 0){
                    target.put("targetUser", user);
                    target.put("targetBoard", board);
                    return target;
                }
            }
        }

        throw new RuntimeException("사용 가능한 데이터가 존재하지 않습니다.");
    }

    private void setMockHttpSession(UserEntity targetUser) {
        assert targetUser != null;

        mockHttpSession.clearAttributes();
        mockHttpSession.setAttribute("USER", UserForm.builder().accountId(targetUser.getAccountId()).password(targetUser.getPassword()).build());
    }

    @Test
    @DisplayName("좋아요 했던 글을 취소한다.")
    void good_cancel() throws Exception {
        GoodContentsHistoryEntity goodContentsHistoryEntity = getGoodContentsHistoryEntity();

        mvc.perform(delete("/api/boards/" + goodContentsHistoryEntity.getBoard().getId() + "/good/" + goodContentsHistoryEntity.getId())
                .session(mockHttpSession))
                .andExpect(status().isOk());
    }

    private GoodContentsHistoryEntity getGoodContentsHistoryEntity() {
        Map<String, Object> target = getTargetUserAndBoard();
        BoardEntity targetBoard = (BoardEntity) target.get("targetBoard");
        UserEntity targetUser = (UserEntity) target.get("targetUser");

        assert targetBoard != null;
        assert targetUser != null;

        GoodContentsHistoryEntity goodContentsHistoryEntity = goodContentsHistoryRepository.save(
                GoodContentsHistoryEntity.builder()
                        .board(targetBoard)
                        .user(targetUser)
                        .build()
        );
        setMockHttpSession(targetUser);

        return goodContentsHistoryEntity;
    }

    @Test
    @DisplayName("자신이 작성한 글에 좋아요 시도 시 예외가 발생한다.")
    void add_like_exception_test() throws Exception {
        BoardEntity newBoard = BoardEntity.builder().contents("contents").title("title").writer(testUser).build();
        boardRepository.save(newBoard);

        mvc.perform(post("/api/boards/" + newBoard.getId() + "/good")
                .session(mockHttpSession))
                .andExpect(result -> assertEquals(result.getResolvedException().getClass().getCanonicalName(), FreeBoardException.class.getCanonicalName()))
                .andExpect(result -> assertEquals(result.getResolvedException().getMessage(), GoodContentsHistoryExceptionType.CANNOT_LIKE_OWN_WRITING.getErrorMessage()))
                .andExpect(status().isOk());
    }
}
