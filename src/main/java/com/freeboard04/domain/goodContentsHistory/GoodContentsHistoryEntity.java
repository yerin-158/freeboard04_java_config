package com.freeboard04.domain.goodContentsHistory;

import com.freeboard04.domain.BaseEntity;
import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.user.UserEntity;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Entity
@Table(name="good_contents_history")
public class GoodContentsHistoryEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "boardId", nullable = false)
    private BoardEntity board;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

}
