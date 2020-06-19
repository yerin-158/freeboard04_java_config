package com.freeboard04.domain.board;

import com.freeboard04.api.board.BoardForm;
import com.freeboard04.api.user.UserForm;
import com.freeboard04.domain.board.entity.specs.BoardSpecs;
import com.freeboard04.domain.board.enums.BoardExceptionType;
import com.freeboard04.domain.board.enums.SearchType;
import com.freeboard04.domain.goodContentsHistory.GoodContentsHistoryEntity;
import com.freeboard04.domain.goodContentsHistory.GoodContentsHistoryRepository;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import com.freeboard04.domain.user.enums.UserExceptionType;
import com.freeboard04.domain.user.specification.HaveAdminRoles;
import com.freeboard04.domain.user.specification.IsWriterEqualToUserLoggedIn;
import com.freeboard04.util.PageUtil;
import com.freeboard04.util.exception.FreeBoardException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class BoardService {

    private BoardRepository boardRepository;
    private UserRepository userRepository;
    private GoodContentsHistoryRepository goodContentsHistoryRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository, UserRepository userRepository, GoodContentsHistoryRepository goodContentsHistoryRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.goodContentsHistoryRepository = goodContentsHistoryRepository;
    }

    public Page<BoardEntity> get(Pageable pageable) {
        return boardRepository.findAll(PageUtil.convertToZeroBasePageWithSort(pageable));
    }

    public BoardEntity post(BoardForm boardForm, UserForm userForm) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        return boardRepository.save(boardForm.convertBoardEntity(user));
    }

    public void update(BoardForm boardForm, UserForm userForm, long id) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = Optional.of(boardRepository.findById(id).get()).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        if (IsWriterEqualToUserLoggedIn.confirm(target.getWriter(), user) == false && HaveAdminRoles.confirm(user) == false) {
            throw new FreeBoardException(BoardExceptionType.NO_QUALIFICATION_USER);
        }

        target.update(boardForm.convertBoardEntity(target.getWriter()));
    }

    public void delete(long id, UserForm userForm) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = Optional.of(boardRepository.findById(id).get()).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        if (IsWriterEqualToUserLoggedIn.confirm(target.getWriter(), user) == false && HaveAdminRoles.confirm(user) == false) {
            throw new FreeBoardException(BoardExceptionType.NO_QUALIFICATION_USER);
        }

        boardRepository.deleteById(id);
    }

    public Page<BoardEntity> search(Pageable pageable, String keyword, SearchType type) {
        if (type.equals(SearchType.WRITER)) {
            List<UserEntity> userEntityList = userRepository.findAllByAccountIdLike("%" + keyword + "%");
            return boardRepository.findAllByWriterIn(userEntityList, PageUtil.convertToZeroBasePageWithSort(pageable));
        }
        Specification<BoardEntity> spec = Specification.where(BoardSpecs.hasContents(keyword, type))
                .or(BoardSpecs.hasTitle(keyword, type));
        return boardRepository.findAll(spec, PageUtil.convertToZeroBasePageWithSort(pageable));
    }

    public void addGoodPoint(UserForm userForm, long boardId) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = Optional.of(boardRepository.findById(boardId).get()).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        goodContentsHistoryRepository.findByUserAndBoard(user, target).ifPresent(none -> { throw new RuntimeException(); });

        goodContentsHistoryRepository.save(
                GoodContentsHistoryEntity.builder()
                        .board(target)
                        .user(user)
                        .build()
        );
    }

    public void deleteGoodPoint(UserForm userForm, long goodHistoryId, long boardId) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = Optional.of(boardRepository.findById(boardId).get()).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        goodContentsHistoryRepository.findByUserAndBoard(user, target).orElseThrow(() -> new RuntimeException());

        goodContentsHistoryRepository.deleteById(goodHistoryId);
    }
}
