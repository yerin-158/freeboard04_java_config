package com.freeboard04.api.comment;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.comment.CommentEntity;
import com.freeboard04.domain.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentForm {

    private String contents;

    @Builder
    public CommentForm(String contents){
        this.contents = contents;
    }

    public CommentEntity convertCommentEntity(UserEntity writer, BoardEntity board){
        return CommentEntity.builder()
                .writer(writer)
                .contents(this.contents)
                .board(board)
                .build();
    }

}
