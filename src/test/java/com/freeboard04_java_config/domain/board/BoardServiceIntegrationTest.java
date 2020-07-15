package com.freeboard04_java_config.domain.board;

import com.freeboard04_java_config.api.board.BoardDto;
import com.freeboard04_java_config.api.user.UserForm;
import com.freeboard04_java_config.config.ApplicationConfig;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryEntity;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryRepository;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Transactional
public class BoardServiceIntegrationTest {

    @Autowired
    private BoardService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GoodContentsHistoryRepository goodContentsHistoryRepository;

    @Test
    @DisplayName("BoardDto가 제대로 만들어지는지 확인한다.")
    void combineBoardDto_test() {
        UserEntity writer = userRepository.findAll().get(0);
        UserEntity userLoggedIn = userRepository.findAll().get(1);

        int pageSize = (int) (Math.random() * 5) + 1;
        int likeContentsCount = (int) (Math.random() * pageSize) + 1;

        List<Long> likeBoardIds = new ArrayList<>();
        List<Long> likeBoardHistoryIds = new ArrayList<>();
        for (int index = 0; index < pageSize; ++index) {
            BoardEntity boardEntity
                    = boardRepository.save(BoardEntity.builder()
                                                .writer(writer)
                                                .title("combineBoardDto 테스트" + index)
                                                .contents("내용")
                                                .build());

            if (index < likeContentsCount) {
                likeBoardIds.add(boardEntity.getId());
                GoodContentsHistoryEntity goodContentsHistoryEntity
                        = goodContentsHistoryRepository.save(GoodContentsHistoryEntity.builder()
                                                                    .board(boardEntity)
                                                                    .user(userLoggedIn)
                                                                    .build());
                likeBoardHistoryIds.add(goodContentsHistoryEntity.getId());
            }
        }

        List<BoardDto> target = sut.get(
                PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")),
                Optional.of(UserForm.builder().accountId(userLoggedIn.getAccountId()).build())
        ).getContents();

        List<BoardDto> likeTarget = target.stream().filter(dto -> dto.isLike()).collect(Collectors.toList());

        assertThat(likeTarget.size(), equalTo(likeContentsCount));
        for (BoardDto boardDto : likeTarget) {
            assertThat(likeBoardIds, hasItem(boardDto.getId()));
            assertThat(likeBoardHistoryIds, hasItems(boardDto.getGoodHistoryId()));
        }
    }

}
