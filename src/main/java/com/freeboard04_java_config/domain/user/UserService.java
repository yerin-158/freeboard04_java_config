package com.freeboard04_java_config.domain.user;

import com.freeboard04_java_config.api.user.UserDto;
import com.freeboard04_java_config.api.user.UserForm;
import com.freeboard04_java_config.domain.user.enums.UserExceptionType;
import com.freeboard04_java_config.domain.user.enums.UserRole;
import com.freeboard04_java_config.util.exception.FreeBoardException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRole findUserRole(UserForm user){
        return userRepository.findByAccountId(user.getAccountId()).getRole();
    }

    public void join(UserForm user) {
        if (userRepository.findByAccountId(user.getAccountId()) != null){
            throw new FreeBoardException(UserExceptionType.DUPLICATED_USER);
        }
        UserEntity newUser = user.convertUserEntity();
        newUser.setRole(UserRole.NORMAL);
        userRepository.save(newUser);
    }

    public UserDto login(UserForm user) {
        UserEntity userEntity = userRepository.findByAccountId(user.getAccountId());
        if (userEntity == null){
            throw new FreeBoardException(UserExceptionType.NOT_FOUND_USER);
        }
        if (userEntity.getPassword().equals(user.getPassword()) == false){
            throw new FreeBoardException(UserExceptionType.WRONG_PASSWORD);
        }
        return UserDto.of(userEntity); // react-front에서 사용하기 위해 수정
    }

}
