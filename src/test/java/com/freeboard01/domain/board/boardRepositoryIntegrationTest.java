package com.freeboard01.domain.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml"})
@Transactional
@Rollback(value = false)
public class boardRepositoryIntegrationTest {

    @Autowired
    private BoardRepository sut = null;

    @Test
    public void test1() {
        List<BoardEntity> boardEntityList = new ArrayList<>();
        for (int i = 0; i < 30; ++i) {
            BoardEntity entity = BoardEntity.builder().user("myNameis" + i).title("제목입니다." + i + i + i).password("1234").contents("test data~" + i).build();
            boardEntityList.add(entity);
        }
        sut.saveAll(boardEntityList);
    }

    @Test
    public void updateTest(){
        BoardEntity saveEntity = BoardEntity.builder().user("유저").title("제목입니다^^*").contents("오늘은 날씨가 좋네요").password("123!@#").build();
        sut.save(saveEntity);

        String updateContents = "수정된 데이터입니다.";
        saveEntity.setContents(updateContents);
        BoardEntity updatedEntity = sut.findById(saveEntity.getId()).get();
        assertThat(updatedEntity.getContents(), equalTo(updateContents));
    }

}
