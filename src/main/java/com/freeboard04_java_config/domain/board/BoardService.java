package com.freeboard04_java_config.domain.board;

import com.freeboard04_java_config.api.PageDto;
import com.freeboard04_java_config.api.board.BoardDto;
import com.freeboard04_java_config.api.board.BoardForm;
import com.freeboard04_java_config.api.user.UserForm;
import com.freeboard04_java_config.domain.board.entity.specs.BoardSpecs;
import com.freeboard04_java_config.domain.board.enums.BoardExceptionType;
import com.freeboard04_java_config.domain.board.enums.SearchType;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryEntity;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryMapper;
import com.freeboard04_java_config.domain.goodContentsHistory.GoodContentsHistoryRepository;
import com.freeboard04_java_config.domain.goodContentsHistory.enums.GoodContentsHistoryExceptionType;
import com.freeboard04_java_config.domain.goodContentsHistory.vo.CountGoodContentsHistoryVO;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import com.freeboard04_java_config.domain.user.enums.UserExceptionType;
import com.freeboard04_java_config.domain.user.specification.HaveAdminRoles;
import com.freeboard04_java_config.domain.user.specification.IsWriterEqualToUserLoggedIn;
import com.freeboard04_java_config.util.PageUtil;
import com.freeboard04_java_config.util.exception.FreeBoardException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class BoardService {

    private BoardRepository boardRepository;
    private UserRepository userRepository;
    private GoodContentsHistoryRepository goodContentsHistoryRepository;
    private GoodContentsHistoryMapper goodContentsHistoryMapper;

    @Autowired
    public BoardService(BoardRepository boardRepository, UserRepository userRepository, GoodContentsHistoryRepository goodContentsHistoryRepository, GoodContentsHistoryMapper goodContentsHistoryMapper) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.goodContentsHistoryRepository = goodContentsHistoryRepository;
        this.goodContentsHistoryMapper = goodContentsHistoryMapper;
    }

    public PageDto<BoardDto> get(Pageable pageable, Optional<UserForm> userLoggedIn) {
        Page<BoardEntity> boardEntityPage = boardRepository.findAll(PageUtil.convertToZeroBasePageWithSort(pageable));
        List<BoardEntity> boardEntities = boardEntityPage.getContent();
        List<CountGoodContentsHistoryVO> boardsLikeCounts = goodContentsHistoryMapper.countByBoardIn(boardEntities);

        if (userLoggedIn.isPresent()) {
            UserEntity user = userRepository.findByAccountId(userLoggedIn.get().getAccountId());
            List<CountGoodContentsHistoryVO> boardsLikeCountsByUser = goodContentsHistoryMapper.countByBoardInAndUser(boardEntities, user);
            List<BoardDto> boardDtos = combineBoardDto(boardEntities, boardsLikeCounts, boardsLikeCountsByUser);
            return PageDto.of(boardEntityPage, boardDtos);
        }

        List<BoardDto> boardDtos = combineBoardDto(boardEntities, boardsLikeCounts);
        return PageDto.of(boardEntityPage, boardDtos);
    }

    private List<BoardDto> combineBoardDto(List<BoardEntity> boardEntities, List<CountGoodContentsHistoryVO> boardsLikeCounts) {
        Map<Long, Long> boardsLikeCountsMap = boardsLikeCounts.stream().collect(Collectors.toMap(CountGoodContentsHistoryVO::getGroupId, CountGoodContentsHistoryVO::getLikeCount));

        return boardEntities.stream().map(boardEntity -> {
            BoardDto boardDto = BoardDto.of(boardEntity);
            if (Optional.ofNullable(boardsLikeCountsMap.get(boardDto.getId())).isPresent()){
                boardDto.setLikePoint(boardsLikeCountsMap.get(boardDto.getId()));
            } else {
                boardDto.setLikePoint(0);
            }
            boardDto.setLike(false);
            return boardDto;
        }).collect(Collectors.toList());
    }

    private List<BoardDto> combineBoardDto(List<BoardEntity> boardEntities, List<CountGoodContentsHistoryVO> boardsLikeCounts, List<CountGoodContentsHistoryVO> boardsLikeCountsByUser) {
        Map<Long, Long> boardsLikeCountsMap = boardsLikeCounts.stream().collect(Collectors.toMap(CountGoodContentsHistoryVO::getGroupId, CountGoodContentsHistoryVO::getLikeCount));
        Map<Long, Long> boardsLikeCountsByUserMap = boardsLikeCountsByUser.stream().collect(Collectors.toMap(CountGoodContentsHistoryVO::getGroupId, CountGoodContentsHistoryVO::getGoodContentsHistoryId));

        return boardEntities.stream().map(boardEntity -> {
            BoardDto boardDto = BoardDto.of(boardEntity);
            if (Optional.ofNullable(boardsLikeCountsMap.get(boardDto.getId())).isPresent()){
                boardDto.setLikePoint(boardsLikeCountsMap.get(boardDto.getId()));
            } else {
                boardDto.setLikePoint(0);
            }
            boardDto.setLike(Optional.ofNullable(boardsLikeCountsByUserMap.get(boardDto.getId())).isPresent());
            if (Optional.ofNullable(boardsLikeCountsByUserMap.get(boardDto.getId())).isPresent()) {
                boardDto.setGoodHistoryId(boardsLikeCountsByUserMap.get(boardDto.getId()));
            }
            return boardDto;
        }).collect(Collectors.toList());
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

    public PageDto<BoardDto> search(Pageable pageable, String keyword, SearchType type, UserForm userForm) {
        Page<BoardEntity> boardEntityPage = getBoardEntityPageByKeyword(pageable, keyword, type);

        List<BoardEntity> boardEntities = boardEntityPage.getContent();
        List<CountGoodContentsHistoryVO> boardsLikeCounts = goodContentsHistoryMapper.countByBoardIn(boardEntities);

        UserEntity userLoggedIn = userRepository.findByAccountId(userForm.getAccountId());
        List<CountGoodContentsHistoryVO> boardsLikeCountsByUser = goodContentsHistoryMapper.countByBoardInAndUser(boardEntities, userLoggedIn);

        List<BoardDto> boardDtos = combineBoardDto(boardEntities, boardsLikeCounts, boardsLikeCountsByUser);
        return PageDto.of(boardEntityPage, boardDtos);
    }

    private Page<BoardEntity> getBoardEntityPageByKeyword(Pageable pageable, String keyword, SearchType type) {
        if (type.equals(SearchType.WRITER)) {
            List<UserEntity> userEntityList = userRepository.findAllByAccountIdLike("%" + keyword + "%");
            return boardRepository.findAllByWriterIn(userEntityList, PageUtil.convertToZeroBasePageWithSort(pageable));
        } else {
            Specification<BoardEntity> spec = Specification.where(BoardSpecs.hasContents(keyword, type))
                    .or(BoardSpecs.hasTitle(keyword, type));
            return boardRepository.findAll(spec, PageUtil.convertToZeroBasePageWithSort(pageable));
        }
    }

    public GoodContentsHistoryEntity addGoodPoint(UserForm userForm, long boardId) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = Optional.of(boardRepository.findById(boardId).get()).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        if (target.getWriter().equals(user)){
            throw new FreeBoardException(GoodContentsHistoryExceptionType.CANNOT_LIKE_OWN_WRITING);
        }

        goodContentsHistoryRepository.findByUserAndBoard(user, target).ifPresent(none -> {
            throw new FreeBoardException(GoodContentsHistoryExceptionType.HISTORY_ALREADY_EXISTS);
        });

        return goodContentsHistoryRepository.save(
                GoodContentsHistoryEntity.builder()
                        .board(target)
                        .user(user)
                        .build()
        );
    }

    public void deleteGoodPoint(UserForm userForm, long goodHistoryId, long boardId) {
        UserEntity user = Optional.of(userRepository.findByAccountId(userForm.getAccountId())).orElseThrow(() -> new FreeBoardException(UserExceptionType.NOT_FOUND_USER));
        BoardEntity target = Optional.of(boardRepository.findById(boardId).get()).orElseThrow(() -> new FreeBoardException(BoardExceptionType.NOT_FOUNT_CONTENTS));

        goodContentsHistoryRepository.findByUserAndBoard(user, target).orElseThrow(() -> new FreeBoardException(GoodContentsHistoryExceptionType.CANNOT_FIND_HISTORY));

        goodContentsHistoryRepository.deleteById(goodHistoryId);
    }
}
