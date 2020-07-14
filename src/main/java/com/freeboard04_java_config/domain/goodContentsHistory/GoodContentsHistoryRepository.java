package com.freeboard04_java_config.domain.goodContentsHistory;

import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodContentsHistoryRepository extends JpaRepository<GoodContentsHistoryEntity, Long> {
    Optional<GoodContentsHistoryEntity> findByUserAndBoard(UserEntity user, BoardEntity board);
    List<GoodContentsHistoryEntity> findAllByUserAndBoard(UserEntity user, BoardEntity board);
    int countByBoard(BoardEntity boardEntity);
}
