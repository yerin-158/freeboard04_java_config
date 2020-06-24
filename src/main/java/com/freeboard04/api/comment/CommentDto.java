package com.freeboard04.api.comment;

import com.freeboard04.api.user.UserDto;
import com.freeboard04.domain.comment.CommentEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CommentDto {

    private long id;
    private UserDto writer;
    private String contents;

    private CommentDto(CommentEntity comment){
        this.id = comment.getId();
        this.writer = UserDto.of(comment.getWriter());
        this.contents = comment.getContents();
    }

    public static CommentDto of(CommentEntity comment){
        return new CommentDto(comment);
    }

}
