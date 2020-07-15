package com.freeboard04_java_config.domain.goodContentsHistory;

import com.freeboard04_java_config.config.ApplicationConfig;
import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.board.BoardRepository;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
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

    @Test
    @DisplayName("사용자와 게시글 엔티티를 이용해 좋아요 내역을 가져올 수 있다.")
    void find_test(){
        BoardEntity newContents = BoardEntity.builder().writer(userEntity).build();
        UserEntity loggedUser = userRepository.findAll().stream().filter(user -> user.equals(userEntity) == false).findFirst().get();

        saveNewBoardAndGoodHistory(newContents, loggedUser);

        Optional<GoodContentsHistoryEntity> savedEntity = sut.findByUserAndBoard(loggedUser, newContents);

        assertThat(savedEntity.get(), not(nullValue()));
    }

    private void saveNewBoardAndGoodHistory(BoardEntity newContents, UserEntity loggedUser) {
        boardRepository.save(newContents);
        sut.save(GoodContentsHistoryEntity.builder().user(loggedUser).board(newContents).build());
    }

}
