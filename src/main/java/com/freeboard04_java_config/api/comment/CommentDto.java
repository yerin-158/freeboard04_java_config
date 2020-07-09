package com.freeboard04_java_config.api.comment;

import com.freeboard04_java_config.api.user.UserDto;
import com.freeboard04_java_config.domain.comment.CommentEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class CommentDto {

    private long id;
    private UserDto writer;
    private String contents;
    private LocalDateTime createdAt;

    private CommentDto(CommentEntity comment){
        this.id = comment.getId();
        this.writer = UserDto.of(comment.getWriter());
        this.contents = comment.getContents();
        this.createdAt = comment.getCreatedAt();
    }

    public static CommentDto of(CommentEntity comment){
        return new CommentDto(comment);
    }

}
