package com.freeboard04_java_config.domain.batch;

import com.freeboard04_java_config.domain.BaseEntity;
import com.freeboard04_java_config.domain.board.BoardEntity;
import lombok.Builder;

import javax.persistence.*;

@Entity
@Table(name = "hourlyPopularBoardEntity")
public class HourlyPopularBoardEntity extends BaseEntity {

    @Column
    private long totalScore;

    @Column
    private long likeCount;

    @Column
    private long commentCount;

    @OneToOne
    @JoinColumn(name = "boardId")
    private BoardEntity board;

    @Builder
    public HourlyPopularBoardEntity(long totalScore, long likeCount, long commentCount, BoardEntity board){
        this.totalScore = totalScore;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.board = board;
    }

}
