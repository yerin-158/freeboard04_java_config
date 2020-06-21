package com.freeboard04.domain.board;

import com.freeboard04.api.PageDto;
import com.freeboard04.api.board.BoardDto;
import com.freeboard04.api.user.UserForm;
import com.freeboard04.domain.board.entity.specs.BoardSpecs;
import com.freeboard04.domain.board.enums.BoardExceptionType;
import com.freeboard04.domain.board.enums.SearchType;
import com.freeboard04.domain.goodContentsHistory.GoodContentsHistoryEntity;
import com.freeboard04.domain.goodContentsHistory.GoodContentsHistoryMapper;
import com.freeboard04.domain.goodContentsHistory.GoodContentsHistoryRepository;
import com.freeboard04.domain.goodContentsHistory.enums.GoodContentsHistoryExceptionType;
import com.freeboard04.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import com.freeboard04.domain.user.enums.UserExceptionType;
import com.freeboard04.domain.user.enums.UserRole;
import com.freeboard04.util.PageUtil;
import com.freeboard04.util.exception.FreeBoardException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml"})
public class BoardServiceUnitTest {

    @InjectMocks
    private BoardService sut;

    @Mock
    private BoardRepository mockBoardRepo;

    @Mock
    private UserRepository mockUserRepo;

    @Mock
    private GoodContentsHistoryRepository mockGoodHistoryRepo;

    @Mock
    private GoodContentsHistoryMapper mockGoodHistoryMapper;

    @Test
    @DisplayName("로그인한 유저와 글을 작성한 유저가 다를 경우 삭제를 진행하지 않는다.")
    public void delete1() {
        UserEntity writer = UserEntity.builder().accountId("mockUser").password("mockPass").build();
        UserForm userLoggedIn = UserForm.builder().accountId("wrongUser").password("wrongUser").build();
        BoardEntity boardEntity = BoardEntity.builder().contents("contents").title("title").writer(writer).build();

        given(mockUserRepo.findByAccountId(anyString())).willReturn(userLoggedIn.convertUserEntity());
        given(mockBoardRepo.findById(anyLong())).willReturn(Optional.of(boardEntity));

        Throwable e = assertThrows(FreeBoardException.class,
                () -> sut.delete(anyLong(), userLoggedIn)
        );

        assertEquals(e.getMessage(), BoardExceptionType.NO_QUALIFICATION_USER.getErrorMessage());
        verify(mockBoardRepo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("로그인한 유저와 글을 작성한 유저가 동일할 경우 삭제를 수행한다.")
    public void delete2() {
        final String PASSWORD = "myPass";

        UserForm userForm = UserForm.builder().accountId("mockUser").password(PASSWORD).build();
        UserEntity userLoggedIn = userForm.convertUserEntity();
        BoardEntity boardEntity = BoardEntity.builder().writer(userLoggedIn).contents("contents").title("title").build();

        given(mockUserRepo.findByAccountId(anyString())).willReturn(userLoggedIn);
        given(mockBoardRepo.findById(anyLong())).willReturn(Optional.of(boardEntity));
        doNothing().when(mockBoardRepo).deleteById(anyLong());

        sut.delete(anyLong(), userForm);
        verify(mockBoardRepo, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("관리자 계정일 경우 삭제를 수행한다.")
    public void delete3() {
        UserForm userLoggedIn = UserForm.builder().accountId("admin").build();
        UserEntity userLoggedInEntity = userLoggedIn.convertUserEntity();
        userLoggedInEntity.setRole(UserRole.ADMIN);
        UserEntity writer = UserEntity.builder().accountId("mockUser").password("mockPass").build();

        BoardEntity boardEntity = BoardEntity.builder().writer(writer).contents("contents").title("title").build();

        given(mockUserRepo.findByAccountId(anyString())).willReturn(userLoggedInEntity);
        given(mockBoardRepo.findById(anyLong())).willReturn(Optional.of(boardEntity));

        sut.delete(anyLong(), userLoggedIn);
        verify(mockBoardRepo, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("이미 좋아요한 내역이 있는 글에 좋아요를 추가하는 시도를 할 경우, 예외를 발생시킨다.")
    public void addGoodExceptionTest() {
        given(mockUserRepo.findByAccountId(anyString())).willReturn(new UserEntity());
        given(mockBoardRepo.findById(anyLong())).willReturn(Optional.of(new BoardEntity()));
        given(mockGoodHistoryRepo.findByUserAndBoard(any(), any())).willReturn(Optional.of(new GoodContentsHistoryEntity()));

        Throwable e = assertThrows(FreeBoardException.class,
                () -> sut.addGoodPoint(UserForm.builder().accountId("mock").build(), anyLong())
        );

        assertEquals(GoodContentsHistoryExceptionType.HISTORY_ALREADY_EXISTS.getErrorMessage(), e.getMessage());
        verify(mockGoodHistoryRepo, never()).save(any());
    }

    @Test
    @DisplayName("좋아요한 내역이 없는 글에 좋아요를 취소하려는 시도를 할 경우, 예외를 발생시킨다.")
    public void cancelGoodExceptionTest() {
        given(mockUserRepo.findByAccountId(anyString())).willReturn(new UserEntity());
        given(mockBoardRepo.findById(anyLong())).willReturn(Optional.of(new BoardEntity()));
        given(mockGoodHistoryRepo.findByUserAndBoard(any(), any())).willReturn(Optional.ofNullable(null));

        Throwable e = assertThrows(FreeBoardException.class,
                () -> sut.deleteGoodPoint(UserForm.builder().accountId("mock").build(), 1L, 2L)
        );

        assertEquals(GoodContentsHistoryExceptionType.CANNOT_FIND_HISTORY.getErrorMessage(), e.getMessage());
        verify(mockGoodHistoryRepo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("로그인하지 않은 경우에 데이터를 요청하면 isLike는 false로 셋팅된다.")
    public void newGetTest1() {
        long groupId = 1L;
        long likePoint = 10L;
        int pageNumber = 1;
        int pageSize = 1;

        Page<BoardEntity> spyBoardEntityPage = getSpyBoardEntityPage(groupId, pageNumber, pageSize);
        List<CountGoodContentsHistoryVO> spyCountGoodHistory = getSpyCountGoodContentsHistoryVOS(groupId, likePoint);

        given(mockBoardRepo.findAll(PageUtil.convertToZeroBasePage(PageRequest.of(pageNumber, pageSize)))).willReturn(spyBoardEntityPage);
        given(mockGoodHistoryMapper.countByBoardIn(anyList())).willReturn(spyCountGoodHistory);

        PageDto<BoardDto> result = sut.get(PageRequest.of(pageNumber, pageSize), Optional.empty());

        assertThat(result.getContents().get(0).getLikePoint(), equalTo(likePoint));
        assertThat(result.getContents().get(0).isLike(), equalTo(false));
    }

    @Test
    @DisplayName("로그인 경우에 데이터를 요청하면 좋아요한 경우 isLike는 true로 셋팅된다.")
    public void newGetTest2() {
        long groupId = 1L;
        long likePoint = 10L;
        int pageNumber = 1;
        int pageSize = 1;

        Page<BoardEntity> spyBoardEntityPage = getSpyBoardEntityPage(groupId, pageNumber, pageSize);
        List<CountGoodContentsHistoryVO> spyCountGoodHistory = getSpyCountGoodContentsHistoryVOS(groupId, likePoint);

        given(mockBoardRepo.findAll(PageUtil.convertToZeroBasePage(PageRequest.of(pageNumber, pageSize)))).willReturn(spyBoardEntityPage);
        given(mockGoodHistoryMapper.countByBoardIn(anyList())).willReturn(spyCountGoodHistory);
        given(mockUserRepo.findByAccountId(anyString())).willReturn(new UserEntity());
        given(mockGoodHistoryMapper.countByBoardInAndUser(anyList(), any())).willReturn(spyCountGoodHistory);

        PageDto<BoardDto> result = sut.get(PageRequest.of(pageNumber, pageSize), Optional.of(UserForm.builder().accountId("mockTest").build()));

        assertThat(result.getContents().get(0).getLikePoint(), equalTo(likePoint));
        assertThat(result.getContents().get(0).isLike(), equalTo(true));
    }

    @Test
    @DisplayName("작성자로 검색한 경우 - 데이터가 정확하게 합쳐지는지 확인한다.")
    public void searchTest1() {
        long groupId = 1L;
        long likePoint = 10L;
        int pageNumber = 1;
        int pageSize = 1;

        Page<BoardEntity> spyBoardEntityPage = getSpyBoardEntityPage(groupId, pageNumber, pageSize);
        List<CountGoodContentsHistoryVO> spyCountGoodHistory = getSpyCountGoodContentsHistoryVOS(groupId, likePoint);

        given(mockUserRepo.findAllByAccountIdLike(anyString())).willReturn(Lists.emptyList());
        given(mockBoardRepo.findAllByWriterIn(Lists.emptyList(), PageUtil.convertToZeroBasePage(PageRequest.of(pageNumber, pageSize)))).willReturn(spyBoardEntityPage);
        given(mockGoodHistoryMapper.countByBoardIn(anyList())).willReturn(spyCountGoodHistory);
        given(mockUserRepo.findByAccountId(anyString())).willReturn(new UserEntity());
        given(mockGoodHistoryMapper.countByBoardInAndUser(anyList(), any())).willReturn(spyCountGoodHistory);

        PageDto<BoardDto> result = sut.search(PageRequest.of(pageNumber, pageSize), anyString(), SearchType.WRITER,  UserForm.builder().accountId("mockTest").build());

        assertThat(result.getContents().get(0).getLikePoint(), equalTo(likePoint));
        assertThat(result.getContents().get(0).isLike(), equalTo(true));
    }

    private Page<BoardEntity> getSpyBoardEntityPage(long groupId, int pageNumber, int pageSize) {
        return spy(new Page<BoardEntity>() {
            @Override
            public int getTotalPages() {
                return 0;
            }

            @Override
            public long getTotalElements() {
                return 0;
            }

            @Override
            public <U> Page<U> map(Function<? super BoardEntity, ? extends U> converter) {
                return null;
            }

            @Override
            public int getNumber() {
                return pageNumber;
            }

            @Override
            public int getSize() {
                return pageSize;
            }

            @Override
            public int getNumberOfElements() {
                return 0;
            }

            @Override
            public List<BoardEntity> getContent() {
                List<BoardEntity> boardEntities = spy(new ArrayList<>());
                BoardEntity boardEntity = BoardEntity.builder().writer(new UserEntity()).build();
                boardEntity.setId(groupId);
                boardEntities.add(boardEntity);

                return boardEntities;
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public Sort getSort() {
                return Sort.unsorted();
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public Iterator<BoardEntity> iterator() {
                return null;
            }
        });
    }

    private List<CountGoodContentsHistoryVO> getSpyCountGoodContentsHistoryVOS(long groupId, long likeCount) {
        List<CountGoodContentsHistoryVO> countGoodContentsHistoryVOS = spy(new ArrayList<>());
        CountGoodContentsHistoryVO vo = new CountGoodContentsHistoryVO();
        vo.setGroupId(groupId);
        vo.setLikeCount(likeCount);
        countGoodContentsHistoryVOS.add(vo);

        return countGoodContentsHistoryVOS;
    }
}
