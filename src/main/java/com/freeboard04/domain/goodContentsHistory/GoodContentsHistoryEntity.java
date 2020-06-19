package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.BaseEntity;
import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Entity
@Table(name="good_contents_history")
@NoArgsConstructor
public class GoodContentsHistoryEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "boardId", nullable = false)
    private BoardEntity board;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Builder
    public GoodContentsHistoryEntity(BoardEntity board, UserEntity user){
        this.board = board;
        this.user = user;
    }
}
