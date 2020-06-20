package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import com.freeboard04.domain.user.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodContentsHistoryMapper {
    List<CountGoodContentsHistoryVO> countByBoardIn(@Param("boards") List<BoardEntity> boards);
    List<CountGoodContentsHistoryVO> countByBoardInAndUser(@Param("boards") List<BoardEntity> boards, @Param("user")UserEntity user);
}
