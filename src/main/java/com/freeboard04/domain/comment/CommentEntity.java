package com.freeboard04.domain.comment;

import com.freeboard04.api.comment.CommentForm;
import com.freeboard04.domain.BaseEntity;
import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "comment")
@NoArgsConstructor
@DynamicUpdate
public class CommentEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "boardId")
    private BoardEntity board;

    @OneToOne
    @JoinColumn(name = "writerId")
    private UserEntity writer;

    @Column
    private String contents;

    @Builder
    public CommentEntity(BoardEntity board, UserEntity writer, String contents) {
        this.board = board;
        this.writer = writer;
        this.contents = contents;
    }

    public void update(CommentForm newComment) {
        this.contents = newComment.getContents();
    }

}
