package com.freeboard04.domain.comment;

import com.freeboard04.domain.board.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    Page<CommentEntity> findAllByBoard(BoardEntity board);
}
