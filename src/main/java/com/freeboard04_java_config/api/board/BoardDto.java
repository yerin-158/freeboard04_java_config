package com.freeboard04_java_config.api.board;

import com.freeboard04_java_config.api.user.UserDto;
import com.freeboard04_java_config.domain.board.BoardEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class BoardDto{

    private long id;
    private UserDto writer;
    private String contents;
    private String title;
    private LocalDateTime createdAt;
    @Setter
    private long likePoint;
    @Setter
    private boolean isLike;
    @Setter
    private long goodHistoryId;

    public BoardDto(BoardEntity board) {
        this.writer = UserDto.of(board.getWriter());
        this.id = board.getId();
        this.contents = board.getContents();
        this.title = board.getTitle();
        this.createdAt = board.getCreatedAt();
    }

    public static BoardDto of(BoardEntity board) {
        return new BoardDto(board);
    }
}
