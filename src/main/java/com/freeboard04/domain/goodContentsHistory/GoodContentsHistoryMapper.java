package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodContentsHistoryMapper {
    List<CountGoodContentsHistoryVO> countByBoardIn(@Param("boards") List<BoardEntity> boards);
}
