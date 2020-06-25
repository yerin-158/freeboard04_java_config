package com.freeboard04.api.comment;

import com.freeboard04.api.PageDto;
import com.freeboard04.api.user.UserForm;
import com.freeboard04.domain.comment.CommentEntity;
import com.freeboard04.domain.comment.CommentService;
import com.freeboard04.domain.user.enums.UserExceptionType;
import com.freeboard04.util.PageUtil;
import com.freeboard04.util.exception.FreeBoardException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {

    private final HttpSession httpSession;
    private final CommentService commentService;

    @GetMapping(params = {"boardId"})
    public ResponseEntity<PageDto<CommentDto>> get(@PageableDefault(page = 1, size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                   @RequestParam long boardId) {
        Page<CommentEntity> commentEntityPage = commentService.get(PageUtil.convertToZeroBasePageWithSort(pageable), boardId);
        List<CommentDto> commentEntities = commentEntityPage.getContent().stream().map(commentEntity -> CommentDto.of(commentEntity)).collect(Collectors.toList());

        return ResponseEntity.ok(PageDto.of(commentEntityPage, commentEntities));
    }

    @PostMapping(params = {"boardId"})
    public ResponseEntity<CommentDto> post(@RequestBody CommentForm commentForm, @RequestParam long boardId) {
        if (httpSession.getAttribute("USER") == null) {
            throw new FreeBoardException(UserExceptionType.LOGIN_INFORMATION_NOT_FOUND);
        }
        CommentDto savedComment = commentService.save(commentForm, (UserForm) httpSession.getAttribute("USER"), boardId);
        return ResponseEntity.ok(savedComment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> put(@RequestBody CommentForm commentForm, @PathVariable long id) {
        if (httpSession.getAttribute("USER") == null) {
            throw new FreeBoardException(UserExceptionType.LOGIN_INFORMATION_NOT_FOUND);
        }
        CommentDto updatedComment = commentService.update(commentForm, (UserForm) httpSession.getAttribute("USER"), id);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        if (httpSession.getAttribute("USER") == null) {
            throw new FreeBoardException(UserExceptionType.LOGIN_INFORMATION_NOT_FOUND);
        }
        commentService.delete((UserForm) httpSession.getAttribute("USER"), id);
    }

}
