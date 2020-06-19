package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.board.BoardRepository;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml"})
@Transactional
class GoodContentsHistoryRepositoryTest {

    @Autowired
    private GoodContentsHistoryRepository sut;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    private BoardEntity boardEntity;
    private UserEntity userEntity;
    private GoodContentsHistoryEntity goodContentsHistoryEntity;

    @BeforeEach
    public void init() {
        userEntity = userRepository.findAll().get(0);
        List<BoardEntity> boardEntities = boardRepository.findAll();
        for (BoardEntity entity : boardEntities) {
            if (entity.getWriter().equals(userEntity) == false) {
                boardEntity = entity;
                break;
            }
        }
        goodContentsHistoryEntity = GoodContentsHistoryEntity.builder().board(boardEntity).user(userEntity).build();
    }

    @Test
    void insert_test() {
        sut.save(goodContentsHistoryEntity);

        GoodContentsHistoryEntity savedEntity = sut.findById(goodContentsHistoryEntity.getId()).get();

        assertEquals(goodContentsHistoryEntity, savedEntity);
    }

    @Test
    void delete_test() {
        sut.save(goodContentsHistoryEntity);

        sut.delete(goodContentsHistoryEntity);
        Optional<GoodContentsHistoryEntity> deletedEntity = sut.findById(goodContentsHistoryEntity.getId());

        assertThat(deletedEntity.isPresent(), equalTo(false));
    }

    @Test
    @DisplayName("특정 게시글의 좋아요 개수를 가져온다.")
    void good_counting_test() {
        int goodCount = getGoodCount();

        BoardEntity newBoard = BoardEntity.builder().contents(LocalDateTime.now() + "-test").title(LocalDateTime.now() + "-test").writer(userEntity).build();
        insertGoodContentsHistory(newBoard, goodCount);

        int findCount = sut.countByBoard(newBoard);

        assertThat(findCount, equalTo(goodCount));
    }

    private int getGoodCount() {
        int max = (int) userRepository.count() - 1;
        return (int) (Math.random() * (max)) + 1;
    }

    private void insertGoodContentsHistory(BoardEntity newBoard, int goodCount) {
        boardRepository.save(newBoard);
        List<UserEntity> userEntities = userRepository.findAll().stream().filter(user -> user.equals(userEntity) == false).collect(Collectors.toList());
        for (int i = 0; i < goodCount; ++i) {
            sut.save(GoodContentsHistoryEntity.builder().user(userEntities.get(i)).board(newBoard).build());
        }
    }

}
