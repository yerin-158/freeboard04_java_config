package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.board.BoardRepository;
import com.freeboard04.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml"})
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

        for (CountGoodContentsHistoryVO count : counts){
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

    private List<BoardEntity> getBoards(int size){
        List<BoardEntity> boards = new ArrayList<>();
        for (int i=1 ;i<=size; ++i){
            boards.add(BoardEntity.builder().contents(LocalDateTime.now() + "-test"+i).title(LocalDateTime.now() + "-test"+i).writer(userEntity).build());
        }
        return boards;
    }

}
