package com.freeboard04_java_config.api.comment;

import com.freeboard04_java_config.domain.board.BoardEntity;
import com.freeboard04_java_config.domain.comment.CommentEntity;
import com.freeboard04_java_config.domain.user.UserEntity;
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
