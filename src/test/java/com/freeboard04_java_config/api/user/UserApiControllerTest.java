package com.freeboard04_java_config.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeboard04_java_config.config.ApplicationConfig;
import com.freeboard04_java_config.config.WebConfig;
import com.freeboard04_java_config.domain.user.UserEntity;
import com.freeboard04_java_config.domain.user.UserRepository;
import com.freeboard04_java_config.domain.user.enums.UserExceptionType;
import com.freeboard04_java_config.util.exception.FreeBoardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationConfig.class, WebConfig.class})
@Transactional
@WebAppConfiguration
public class UserApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void initMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    private String randomId(){
        String id = "";
        for(int i = 0; i < 10; i++) {
            double dValue = Math.random();
            if(i%2 == 0) {
                id += (char) ((dValue * 26) + 65);   // 대문자
                continue;
            }
            id += (char)((dValue * 26) + 97); // 소문자
        }
        return id;
    }

    @Test
    @DisplayName("동일한 아이디를 가진 유저가 없으면 가입에 성공한다.")
    public void joinTest1() throws Exception {
        UserForm userForm = UserForm.builder().accountId(randomId()).password("password").build();
        mvc.perform(post("/api/users")
                .content(objectMapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동일한 아이디를 가진 유저가 있으면 가입에 실패한다.")
    public void joinTest2() throws Exception {
        UserEntity userEntity = userRepository.findAll().get(0);
        UserForm userForm = UserForm.builder().accountId(userEntity.getAccountId()).password("password").build();
        mvc.perform(post("/api/users")
                .content(objectMapper.writeValueAsString(userForm))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(result -> assertEquals(result.getResolvedException().getMessage(), UserExceptionType.DUPLICATED_USER.getErrorMessage()))
                .andExpect(result -> assertEquals(result.getResolvedException().getClass().getCanonicalName(), FreeBoardException.class.getCanonicalName()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호를 올바르게 입력하면 로그인되고, 세션에 저장된다.")
    public void loginTest1() throws Exception {
        UserEntity userEntity = userRepository.findAll().get(0);
        UserForm userForm = UserForm.builder().accountId(userEntity.getAccountId()).password(userEntity.getPassword()).build();

        mvc.perform(post("/api/users?type=LOGIN")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userForm)))
                .andExpect(request().sessionAttribute("USER", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호를 올바르게 입력하지 않으면 로그인 거부된다.")
    public void loginTest2() throws Exception {
        UserEntity userEntity = userRepository.findAll().get(0);
        UserForm userForm = UserForm.builder().accountId(userEntity.getAccountId()).password(userEntity.getPassword()+"wrongPass").build();

        mvc.perform(post("/api/users?type=LOGIN")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userForm)))
                .andExpect(request().sessionAttribute("USER", nullValue()))
                .andExpect(result -> assertEquals(result.getResolvedException().getMessage(), UserExceptionType.WRONG_PASSWORD.getErrorMessage()))
                .andExpect(result -> assertEquals(result.getResolvedException().getClass().getCanonicalName(), FreeBoardException.class.getCanonicalName()))
                .andExpect(status().isOk());
    }



}

