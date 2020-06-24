package com.freeboard04.domain.comment;

import com.freeboard04.api.comment.CommentDto;
import com.freeboard04.api.comment.CommentForm;
import com.freeboard04.api.user.UserForm;
import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.board.BoardRepository;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import com.freeboard04.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml"})
@Transactional
@Rollback(false)
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService sut;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    private UserEntity userEntity;
    private BoardEntity boardEntity;
    private UserForm userForm;
    private CommentForm newComment;

    @BeforeEach
    void init(){
        userEntity = userRepository.findAll().get(0);
        userForm = UserForm.builder().accountId(userEntity.getAccountId()).password(userEntity.getPassword()).build();
        boardEntity = boardRepository.findAll().get(0);

        newComment = CommentForm.builder().contents("무플방지위원회에서 나왔습니다. ^^*").build();
    }

    @Test
    void save_test(){
        CommentDto commentDto = sut.save(newComment, userForm, boardEntity.getId());
        CommentEntity savedComment = commentRepository.findById(commentDto.getId()).get();

        assertThat(savedComment.getId(), equalTo(commentDto.getId()));
    }

    @Test
    void update_test(){
        CommentEntity savedComment = getNewSavedCommentEntity();

        String updateContents = "수정 후 댓글 내용입니다~~";
        CommentForm commentForm = CommentForm.builder().contents(updateContents).build();

        CommentDto updatedComment = sut.update(commentForm, userForm, savedComment.getId());

        assertThat(updatedComment.getContents(), equalTo(updateContents));
    }

    private CommentEntity getNewSavedCommentEntity() {
        return commentRepository.save(newComment.convertCommentEntity(userEntity, boardEntity));
    }

    @Test
    void delete_test(){
        CommentEntity savedComment = getNewSavedCommentEntity();

        sut.delete(userForm, savedComment.getId());

        Optional<CommentEntity> deletedEntity = commentRepository.findById(savedComment.getId());

        assertThat(deletedEntity.isPresent(), equalTo(false));
    }

    @Test
    void get_test(){
        int pageSize = 5;
        int pageNumber = 1;

        Page<CommentEntity> commentEntityPage = sut.get(PageRequest.of(pageNumber, pageSize), boardEntity.getId());

        assertThat(commentEntityPage.getNumber(), equalTo(pageNumber));
        assertThat(commentEntityPage.getSize(), equalTo(pageSize));
    }

}
