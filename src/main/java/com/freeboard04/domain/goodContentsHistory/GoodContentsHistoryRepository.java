package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoodContentsHistoryRepository extends JpaRepository<GoodContentsHistoryEntity, Long> {
    Optional<GoodContentsHistoryEntity> findByUserAndBoard(UserEntity user, BoardEntity board);
    int countByBoard(BoardEntity boardEntity);
}
