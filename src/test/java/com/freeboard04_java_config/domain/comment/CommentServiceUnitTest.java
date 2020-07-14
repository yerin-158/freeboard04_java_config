package com.freeboard04_java_config.domain.comment;

import com.freeboard04_java_config.config.ApplicationContext;
import com.freeboard04_java_config.domain.board.BoardRepository;
import com.freeboard04_java_config.domain.user.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = {ApplicationContext.class})
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
