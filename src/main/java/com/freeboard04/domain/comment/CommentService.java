package com.freeboard04.domain.comment;

import com.freeboard04.api.comment.CommentDto;
import com.freeboard04.api.comment.CommentForm;
import com.freeboard04.api.user.UserForm;
import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.board.BoardRepository;
import com.freeboard04.domain.board.enums.BoardExceptionType;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import com.freeboard04.domain.user.enums.UserExceptionType;
import com.freeboard04.domain.user.specification.HaveAdminRoles;
import com.freeboard04.domain.user.specification.IsWriterEqualToUserLoggedIn;
import com.freeboard04.util.exception.FreeBoardException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CommentService {

    private UserRepository userRepository;
    private CommentRepository commentRepository;
    private BoardRepository boardRepository;

    @Autowired
    public CommentService(UserRepository userRepository, CommentRepository commentRepository, BoardRepository boardRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
    }

    public CommentDto save(CommentForm commentForm, UserForm userForm, long boardId) {
        UserEntity writer = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = boardRepository.findById(boardId).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        CommentEntity savedComment = commentRepository.save(commentForm.convertCommentEntity(writer, target));
        return CommentDto.of(savedComment);
    }

    public Page<CommentEntity> get(Pageable pageable, long boardId) {
        BoardEntity target = boardRepository.findById(boardId).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));
        return commentRepository.findAllByBoard(target, pageable);
    }

    public CommentDto update(CommentForm commentForm, UserForm userForm, long id) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        //TODO : custom exception으로 변경
        CommentEntity target = commentRepository.findById(id).orElseThrow(() -> new RuntimeException());

        if (IsWriterEqualToUserLoggedIn.confirm(target.getWriter(), user) == false) {
            throw new FreeBoardException(BoardExceptionType.NO_QUALIFICATION_USER);
        }

        target.update(commentForm);
        return CommentDto.of(target);
    }

    public void delete(UserForm userForm, long id) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        //TODO : custom exception으로 변경
        CommentEntity target = commentRepository.findById(id).orElseThrow(() -> new RuntimeException());

        if (IsWriterEqualToUserLoggedIn.confirm(target.getWriter(), user) == false && HaveAdminRoles.confirm(user) == false) {
            throw new FreeBoardException(BoardExceptionType.NO_QUALIFICATION_USER);
        }
        commentRepository.delete(target);
    }


}
