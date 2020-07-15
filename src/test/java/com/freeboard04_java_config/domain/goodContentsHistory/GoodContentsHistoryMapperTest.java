package com.freeboard04_java_config.domain.goodContentsHistory;

import com.freeboard04_java_config.config.ApplicationConfig;
import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.board.BoardRepository;
import com.freeboard04_java_config.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Transactional
class GoodContentsHistoryMapperTest {

    @Autowired
    GoodContentsHistoryMapper sut;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    GoodContentsHistoryRepository goodContentsHistoryRepository;

    UserEntity userEntity;

    @BeforeEach
    private void init() {
        userEntity = userRepository.findAll().get(0);
    }

    @Test
    void countByBoardIdIn_test() {
        int goodCount = getGoodCount();
        List<BoardEntity> boards = getBoards(2);
        insertGoodContentsHistory(boards, goodCount);

        List<CountGoodContentsHistoryVO> counts = sut.countByBoardIn(boards);

        for (CountGoodContentsHistoryVO count : counts) {
            assertThat(boards.stream().map(boardEntity -> boardEntity.getId()).collect(Collectors.toList()), hasItem(count.getGroupId()));
            assertThat((int) count.getLikeCount(), equalTo(goodCount));
        }
    }

    private int getGoodCount() {
        int max = (int) userRepository.count() - 1;
        return (int) (Math.random() * (max)) + 1;
    }

    private void insertGoodContentsHistory(List<BoardEntity> newBoards, int goodCount) {
        boardRepository.saveAll(newBoards);
        List<UserEntity> userEntities = userRepository.findAll().stream().filter(user -> user.equals(userEntity) == false).collect(Collectors.toList());
        for (int i = 0; i < goodCount; ++i) {
            for (BoardEntity newBoard : newBoards) {
                goodContentsHistoryRepository.save(GoodContentsHistoryEntity.builder().user(userEntities.get(i)).board(newBoard).build());
            }
        }
    }

    private List<BoardEntity> getBoards(int size) {
        List<BoardEntity> boards = new ArrayList<>();
        for (int i = 1; i <= size; ++i) {
            boards.add(BoardEntity.builder().contents(LocalDateTime.now() + "-test" + i).title(LocalDateTime.now() + "-test" + i).writer(userEntity).build());
        }
        return boards;
    }

    @Test
    @DisplayName("(이전 버전 / 사용안함) 특정 게시물 목록에 대해 해당 사용자가 좋아요 한 내역을 가져온다.")
    @Disabled
    void countByBoardInAndUser_disabled_test(){
        List<BoardEntity> newBoards = getBoards(2);
        BoardEntity likeContents = newBoards.get(0);
        UserEntity userLoggedIn = userRepository.findAll().stream().filter(user -> user.equals(userEntity) == false).findFirst().get();
        saveBoardAndGoodHistory(newBoards, likeContents, userLoggedIn);

        List<CountGoodContentsHistoryVO> vos = sut.countByBoardInAndUser(newBoards, userLoggedIn);

        for (CountGoodContentsHistoryVO vo : vos){
            assertThat(vo.getGroupId(), equalTo(likeContents.getId()));
            assertThat(vo.getLikeCount(), equalTo(1L));
        }
    }

    private void saveBoardAndGoodHistory(List<BoardEntity> newBoards, BoardEntity likeContents, UserEntity userLoggedIn) {
        boardRepository.saveAll(newBoards);
        goodContentsHistoryRepository.save(GoodContentsHistoryEntity.builder().board(likeContents).user(userLoggedIn).build());
    }

    @Test
    @DisplayName("특정 게시물 목록에 대해 해당 사용자가 좋아요 한 내역을 가져온다.")
    void countByBoardInAndUser_test(){
        List<BoardEntity> newBoards = getBoards(2);
        BoardEntity likeContents = newBoards.get(0);
        UserEntity userLoggedIn = getNonAuthorUser();
        GoodContentsHistoryEntity goodContentsHistoryEntity = GoodContentsHistoryEntity.builder().board(likeContents).user(userLoggedIn).build();
        saveBoardAndGoodHistory(newBoards, goodContentsHistoryEntity);

        List<CountGoodContentsHistoryVO> vos = sut.countByBoardInAndUser(newBoards, userLoggedIn);

        for (CountGoodContentsHistoryVO vo : vos){
            assertThat(vo.getGroupId(), equalTo(likeContents.getId()));
            assertThat(vo.getGoodContentsHistoryId(), equalTo(goodContentsHistoryEntity.getId()));
        }
    }

    private UserEntity getNonAuthorUser() {
        return userRepository.findAll().stream().filter(user -> user.equals(userEntity) == false).findFirst().get();
    }

    private void saveBoardAndGoodHistory(List<BoardEntity> newBoards, GoodContentsHistoryEntity goodContentsHistoryEntity) {
        boardRepository.saveAll(newBoards);
        goodContentsHistoryRepository.save(goodContentsHistoryEntity);
    }
}
