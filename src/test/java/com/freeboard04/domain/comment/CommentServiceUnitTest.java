package com.freeboard04.domain.comment;

import com.freeboard04.domain.board.BoardEntity;
import com.freeboard04.domain.board.BoardRepository;
import com.freeboard04.domain.user.UserEntity;
import com.freeboard04.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml"})
class CommentServiceUnitTest {

    @InjectMocks
    private CommentService sut;

    @Mock
    private CommentRepository commentMockRepo;

    @Mock
    private UserRepository userMockRepo;

    @Mock
    private BoardRepository boardMockRepo;

}
