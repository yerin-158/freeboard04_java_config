package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodContentsHistoryRepository extends JpaRepository<GoodContentsHistoryEntity, Long> {
    public Long findByUserAndBoard(UserEntity user, BoardEntity board);
    public int countByBoard(BoardEntity boardEntity);
}
