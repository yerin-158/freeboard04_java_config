package com.freeboard04_java_config.domain.goodContentsHistory;

import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import com.freeboard04_java_config.domain.user.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodContentsHistoryMapper {
    List<CountGoodContentsHistoryVO> countByBoardIn(@Param("boards") List<BoardEntity> boards);
    List<CountGoodContentsHistoryVO> countByBoardInAndUser(@Param("boards") List<BoardEntity> boards, @Param("user")UserEntity user);
}
